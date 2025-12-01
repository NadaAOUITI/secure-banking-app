package com.securebank.service;

import com.securebank.dto.TransferRequestDto;
import com.securebank.dto.TransferResponseDto;
import com.securebank.model.*;
import com.securebank.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransferService {

    // Security limits
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("10000");
    private static final int MAX_TRANSFERS_PER_DAY = 10;
    private static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("50000");
    private static final int DUPLICATE_CHECK_MINUTES = 5;

    // Transfer fees
    private static final BigDecimal INTERNAL_FEE = BigDecimal.ZERO;
    private static final BigDecimal NATIONAL_FEE = new BigDecimal("2.5");
    private static final BigDecimal INTERNATIONAL_FEE_PERCENT = new BigDecimal("0.01");
    private static final BigDecimal INTERNATIONAL_MIN_FEE = new BigDecimal("10");

    private final TransferRepository transferRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    @Autowired
    public TransferService(
            TransferRepository transferRepository,
            BankAccountRepository bankAccountRepository,
            BeneficiaryRepository beneficiaryRepository,
            OtpService otpService,
            EmailService emailService) {
        this.transferRepository = transferRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    public TransferResponseDto initiateTransfer(User user, TransferRequestDto request, HttpServletRequest httpRequest) {
        System.out.println("=== TransferService.initiateTransfer START ===");
        try {
            BankAccount senderAccount = bankAccountRepository.findById(request.getSenderAccountId())
                    .filter(acc -> acc.getUser().getId().equals(user.getId()))
                    .filter(BankAccount::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("Compte source invalide ou non autorisé"));

            Beneficiary beneficiary = beneficiaryRepository.findByIdAndUser(request.getBeneficiaryId(), user)
                    .filter(Beneficiary::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("Bénéficiaire invalide ou non autorisé"));

            validateAmount(request.getAmount());

            BigDecimal fee = calculateFee(beneficiary.getBankType(), request.getAmount());
            BigDecimal totalAmount = request.getAmount().add(fee);

            if (senderAccount.getBalance().compareTo(totalAmount) < 0) {
                throw new IllegalArgumentException("Solde insuffisant. Solde disponible: " + senderAccount.getBalance() + " TND");
            }

            checkDailyLimits(user, request.getAmount());
            checkDuplicateTransfer(user, request.getBeneficiaryId(), request.getAmount());

            Transfer transfer = new Transfer();
            transfer.setUser(user);
            transfer.setSenderAccount(senderAccount);
            transfer.setBeneficiary(beneficiary);
            transfer.setAmount(request.getAmount());
            transfer.setCurrency(request.getCurrency() != null ? request.getCurrency() : "TND");
            transfer.setDescription(sanitizeDescription(request.getDescription()));
            transfer.setReference(generateReference());
            transfer.setFee(fee);
            transfer.setTransferType(mapBankTypeToTransferType(beneficiary.getBankType()));
            transfer.setStatus(Transfer.TransferStatus.OTP_REQUIRED);
            transfer.setIpAddress(truncateIp(getClientIp(httpRequest)));
            transfer.setUserAgent(truncateUserAgent(httpRequest.getHeader("User-Agent")));

            Transfer savedTransfer = transferRepository.save(transfer);
            otpService.generateAndSendOtp(user.getEmail());
            logTransferActivity(savedTransfer, "INITIATED", httpRequest);

            System.out.println("=== TransferService.initiateTransfer SUCCESS ===");
            return new TransferResponseDto(savedTransfer);

        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException in initiateTransfer: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("=== TransferService.initiateTransfer ERROR ===");
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'initiation du virement: " + e.getMessage(), e);
        }
    }

    public TransferResponseDto confirmTransfer(User user, String reference, String otpCode, HttpServletRequest httpRequest) {
        Transfer transfer = transferRepository.findByReferenceAndUser(reference, user)
                .orElseThrow(() -> new IllegalArgumentException("Virement non trouvé"));

        if (transfer.getStatus() != Transfer.TransferStatus.OTP_REQUIRED) {
            throw new IllegalArgumentException("Ce virement a déjà été traité ou annulé");
        }

        if (transfer.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
            transfer.setStatus(Transfer.TransferStatus.CANCELLED);
            transfer.setFailureReason("Virement expiré");
            transferRepository.save(transfer);
            throw new IllegalArgumentException("Le virement a expiré. Veuillez en créer un nouveau.");
        }

        if (!otpService.verifyOtp(user.getEmail(), otpCode)) {
            logTransferActivity(transfer, "OTP_FAILED", httpRequest);
            throw new IllegalArgumentException("Code OTP invalide ou expiré");
        }

        return executeTransfer(transfer, httpRequest);
    }

    private TransferResponseDto executeTransfer(Transfer transfer, HttpServletRequest httpRequest) {
        try {
            transfer.setStatus(Transfer.TransferStatus.PROCESSING);
            transfer.setOtpVerified(true);
            transferRepository.save(transfer);

            BankAccount senderAccount = transfer.getSenderAccount();
            BigDecimal totalAmount = transfer.getTotalAmount();

            if (senderAccount.getBalance().compareTo(totalAmount) < 0) {
                throw new IllegalArgumentException("Solde insuffisant");
            }

            senderAccount.setBalance(senderAccount.getBalance().subtract(totalAmount));
            bankAccountRepository.save(senderAccount);

            if (transfer.getTransferType() == Transfer.TransferType.INTERNAL) {
                Optional<BankAccount> beneficiaryAccount = bankAccountRepository
                        .findByAccountNumber(transfer.getBeneficiary().getAccountNumber());
                if (beneficiaryAccount.isPresent()) {
                    BankAccount benAccount = beneficiaryAccount.get();
                    benAccount.setBalance(benAccount.getBalance().add(transfer.getAmount()));
                    bankAccountRepository.save(benAccount);
                }
            }

            transfer.setStatus(Transfer.TransferStatus.COMPLETED);
            transfer.setExecutedAt(LocalDateTime.now());
            Transfer completedTransfer = transferRepository.save(transfer);

            sendTransferConfirmationEmail(completedTransfer);
            logTransferActivity(completedTransfer, "COMPLETED", httpRequest);

            return new TransferResponseDto(completedTransfer);

        } catch (Exception e) {
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason(e.getMessage());
            transferRepository.save(transfer);
            logTransferActivity(transfer, "FAILED: " + e.getMessage(), httpRequest);
            throw new RuntimeException("Échec du virement: " + e.getMessage());
        }
    }

    public void cancelTransfer(User user, String reference) {
        Transfer transfer = transferRepository.findByReferenceAndUser(reference, user)
                .orElseThrow(() -> new IllegalArgumentException("Virement non trouvé"));

        if (transfer.getStatus() != Transfer.TransferStatus.OTP_REQUIRED &&
                transfer.getStatus() != Transfer.TransferStatus.PENDING) {
            throw new IllegalArgumentException("Ce virement ne peut plus être annulé");
        }

        transfer.setStatus(Transfer.TransferStatus.CANCELLED);
        transfer.setFailureReason("Annulé par l'utilisateur");
        transferRepository.save(transfer);
    }

    public List<TransferResponseDto> getTransferHistory(User user) {
        return transferRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(TransferResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<TransferResponseDto> getTransferHistory(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Long beneficiaryId) {

        List<Transfer> transfers;

        if (startDate != null && endDate != null) {
            transfers = transferRepository.findByUserAndDateRange(user, startDate, endDate);
        } else if (minAmount != null && maxAmount != null) {
            transfers = transferRepository.findByUserAndAmountRange(user, minAmount, maxAmount);
        } else if (beneficiaryId != null) {
            transfers = transferRepository.findByUserAndBeneficiary(user, beneficiaryId);
        } else {
            transfers = transferRepository.findByUserOrderByCreatedAtDesc(user);
        }

        return transfers.stream()
                .map(TransferResponseDto::new)
                .collect(Collectors.toList());
    }

    public Optional<TransferResponseDto> getTransfer(User user, String reference) {
        return transferRepository.findByReferenceAndUser(reference, user)
                .map(TransferResponseDto::new);
    }

    // ==================== HELPERS ====================

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Le montant est requis");
        }
        if (amount.compareTo(MIN_TRANSFER_AMOUNT) < 0) {
            throw new IllegalArgumentException("Le montant minimum est " + MIN_TRANSFER_AMOUNT + " TND");
        }
        if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            throw new IllegalArgumentException("Le montant maximum est " + MAX_TRANSFER_AMOUNT + " TND");
        }
    }

    private void checkDailyLimits(User user, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCount = transferRepository.countTodayTransfersByUser(user, startOfDay);
        if (todayCount >= MAX_TRANSFERS_PER_DAY) {
            throw new IllegalArgumentException("Limite quotidienne de " + MAX_TRANSFERS_PER_DAY + " virements atteinte");
        }

        BigDecimal todayTotal = transferRepository.sumTodayTransfersByUser(user, startOfDay);
        if (todayTotal == null) todayTotal = BigDecimal.ZERO;
        if (todayTotal.add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            BigDecimal remaining = DAILY_TRANSFER_LIMIT.subtract(todayTotal);
            throw new IllegalArgumentException("Limite quotidienne de " + DAILY_TRANSFER_LIMIT + " TND atteinte. Reste disponible: " + remaining + " TND");
        }
    }

    private void checkDuplicateTransfer(User user, Long beneficiaryId, BigDecimal amount) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(DUPLICATE_CHECK_MINUTES);
        if (transferRepository.existsDuplicateTransfer(user, beneficiaryId, amount, since)) {
            throw new IllegalArgumentException("Un virement similaire a été effectué récemment. Veuillez patienter " + DUPLICATE_CHECK_MINUTES + " minutes.");
        }
    }

    private BigDecimal calculateFee(Beneficiary.BankType bankType, BigDecimal amount) {
        if (bankType == null) return BigDecimal.ZERO;
        switch (bankType) {
            case SAME_BANK: return INTERNAL_FEE;
            case NATIONAL: return NATIONAL_FEE;
            case INTERNATIONAL:
                BigDecimal percentFee = amount.multiply(INTERNATIONAL_FEE_PERCENT);
                return percentFee.compareTo(INTERNATIONAL_MIN_FEE) > 0 ? percentFee : INTERNATIONAL_MIN_FEE;
            default: return BigDecimal.ZERO;
        }
    }

    private Transfer.TransferType mapBankTypeToTransferType(Beneficiary.BankType bankType) {
        if (bankType == null) return Transfer.TransferType.NATIONAL;
        switch (bankType) {
            case SAME_BANK: return Transfer.TransferType.INTERNAL;
            case NATIONAL: return Transfer.TransferType.NATIONAL;
            case INTERNATIONAL: return Transfer.TransferType.INTERNATIONAL;
            default: return Transfer.TransferType.NATIONAL;
        }
    }

    private String generateReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")); // 12 chars
        String uuid = UUID.randomUUID().toString().substring(0, 5).toUpperCase(); // 5 chars
        return ("TRF" + timestamp + uuid).substring(0, 20); // Max 20 chars
    }

    private String sanitizeDescription(String description) {
        if (description == null || description.isEmpty()) return "";
        String sanitized = description.trim().replaceAll("[<>\"'&;]", "").replaceAll("\\s+", " ");
        return sanitized.substring(0, Math.min(sanitized.length(), 255));
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) return null;
        return userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent;
    }

    private String truncateIp(String ip) {
        if (ip == null) return null;
        return ip.length() > 45 ? ip.substring(0, 45) : ip;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) return request.getRemoteAddr();
        return xfHeader.split(",")[0].trim();
    }

    private void logTransferActivity(Transfer transfer, String action, HttpServletRequest request) {
        String beneficiaryName = transfer.getBeneficiary() != null ? transfer.getBeneficiary().getName() : "Unknown";
        String userEmail = transfer.getUser() != null ? transfer.getUser().getEmail() : "Unknown";
        System.out.println(String.format(
                "[TRANSFER] %s | Ref: %s | User: %s | Amount: %s %s | Beneficiary: %s | IP: %s",
                action,
                transfer.getReference(),
                userEmail,
                transfer.getAmount(),
                transfer.getCurrency(),
                beneficiaryName,
                getClientIp(request)
        ));
    }

    private void sendTransferConfirmationEmail(Transfer transfer) {
        try {
            String subject = "✅ Virement effectué - " + transfer.getReference();
            String content = buildTransferEmailContent(transfer);
            // emailService.sendHtmlEmail(transfer.getUser().getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Failed to send transfer confirmation email: " + e.getMessage());
        }
    }

    private String buildTransferEmailContent(Transfer transfer) {
        String executedDate = transfer.getExecutedAt() != null
                ? transfer.getExecutedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A";

        return "<html><body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "<div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">" +
                "<h1 style=\"color: white; margin: 0;\">✅ Virement Confirmé</h1></div>" +
                "<div style=\"background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px;\">" +
                "<p><strong>Référence:</strong> " + transfer.getReference() + "</p>" +
                "<p><strong>Bénéficiaire:</strong> " + transfer.getBeneficiary().getName() + "</p>" +
                "<p><strong>Montant:</strong> " + transfer.getAmount() + " " + transfer.getCurrency() + "</p>" +
                "<p><strong>Frais:</strong> " + transfer.getFee() + " " + transfer.getCurrency() + "</p>" +
                "<p><strong>Total débité:</strong> " + transfer.getTotalAmount() + " " + transfer.getCurrency() + "</p>" +
                "<p><strong>Date:</strong> " + executedDate + "</p>" +
                "<hr style=\"border: 1px solid #ddd;\">" +
                "<p style=\"color: #888; font-size: 12px;\">Si vous n'êtes pas à l'origine de ce virement, contactez immédiatement notre service client.</p>" +
                "</div></body></html>";
    }
}
