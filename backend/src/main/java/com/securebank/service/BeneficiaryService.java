package com.securebank.service;

import com.securebank.dto.BeneficiaryRequestDto;
import com.securebank.dto.BeneficiaryResponseDto;
import com.securebank. model.Beneficiary;
import com.securebank. model.User;
import com.securebank. repository.BeneficiaryRepository;
import com.securebank. repository.BankAccountRepository;
import org.springframework.beans. factory.annotation. Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util. regex.Pattern;
import java.util. stream.Collectors;

@Service
@Transactional
public class BeneficiaryService {

    private static final int MAX_BENEFICIARIES_PER_USER = 50;

    // Account number validation patterns
    private static final Pattern SAME_BANK_PATTERN = Pattern.compile("^[0-9]{16,20}$");
    private static final Pattern NATIONAL_PATTERN = Pattern.compile("^[A-Z0-9]{10,24}$");
    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$");

    private final BeneficiaryRepository beneficiaryRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository,
                              BankAccountRepository bankAccountRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Add a new beneficiary for a user
     */
    public BeneficiaryResponseDto addBeneficiary(User user, BeneficiaryRequestDto request) {
        // Check maximum beneficiaries limit
        long currentCount = beneficiaryRepository.countActiveByUser(user);
        if (currentCount >= MAX_BENEFICIARIES_PER_USER) {
            throw new IllegalArgumentException("Nombre maximum de bénéficiaires atteint (" + MAX_BENEFICIARIES_PER_USER + ")");
        }

        // Check if beneficiary already exists
        if (beneficiaryRepository.existsByUserAndAccountNumberAndIsActiveTrue(user, request.getAccountNumber())) {
            throw new IllegalArgumentException("Ce bénéficiaire existe déjà");
        }

        // Validate account number based on bank type
        Beneficiary. BankType bankType = Beneficiary.BankType.valueOf(request.getBankType(). toUpperCase());
        validateAccountNumber(request. getAccountNumber(), bankType, request.getIban());

        // Create beneficiary
        Beneficiary beneficiary = new Beneficiary(user,
                sanitizeInput(request.getName()),
                request.getAccountNumber(). toUpperCase(). replaceAll("\\s", ""),
                bankType);

        beneficiary.setBankName(sanitizeInput(request. getBankName()));
        beneficiary.setBankCode(request.getBankCode());
        beneficiary. setSwiftCode(request. getSwiftCode());
        beneficiary. setIban(request.getIban());
        beneficiary. setCountry(request.getCountry());

        // Auto-verify same bank beneficiaries
        if (bankType == Beneficiary.BankType.SAME_BANK) {
            beneficiary.setVerified(verifyInternalAccount(request.getAccountNumber()));
        }

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return new BeneficiaryResponseDto(saved);
    }

    /**
     * Get all active beneficiaries for a user
     */
    public List<BeneficiaryResponseDto> getBeneficiaries(User user) {
        return beneficiaryRepository.findByUserAndIsActiveTrue(user)
                .stream()
                .map(BeneficiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific beneficiary by ID
     */
    public Optional<BeneficiaryResponseDto> getBeneficiary(User user, Long beneficiaryId) {
        return beneficiaryRepository.findByIdAndUser(beneficiaryId, user)
                .filter(Beneficiary::isActive)
                .map(BeneficiaryResponseDto::new);
    }

    /**
     * Update beneficiary details
     */
    public BeneficiaryResponseDto updateBeneficiary(User user, Long beneficiaryId, BeneficiaryRequestDto request) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUser(beneficiaryId, user)
                .filter(Beneficiary::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Bénéficiaire non trouvé"));

        // Update allowed fields (account number cannot be changed for security)
        beneficiary.setName(sanitizeInput(request.getName()));
        beneficiary.setBankName(sanitizeInput(request.getBankName()));
        beneficiary.setUpdatedAt(LocalDateTime.now());

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return new BeneficiaryResponseDto(saved);
    }

    /**
     * Remove (deactivate) a beneficiary
     */
    public void removeBeneficiary(User user, Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUser(beneficiaryId, user)
                .orElseThrow(() -> new IllegalArgumentException("Bénéficiaire non trouvé"));

        beneficiary.setActive(false);
        beneficiary.setUpdatedAt(LocalDateTime.now());
        beneficiaryRepository.save(beneficiary);
    }

    /**
     * Validate account number format based on bank type
     */
    private void validateAccountNumber(String accountNumber, Beneficiary.BankType bankType, String iban) {
        String cleanAccount = accountNumber.replaceAll("\\s", ""). toUpperCase();

        switch (bankType) {
            case SAME_BANK:
                if (!SAME_BANK_PATTERN.matcher(cleanAccount).matches()) {
                    throw new IllegalArgumentException("Format de numéro de compte invalide pour un compte de même banque");
                }
                break;

            case NATIONAL:
                if (! NATIONAL_PATTERN.matcher(cleanAccount).matches()) {
                    throw new IllegalArgumentException("Format de numéro de compte invalide pour une banque nationale");
                }
                break;

            case INTERNATIONAL:
                if (iban == null || iban.isEmpty()) {
                    throw new IllegalArgumentException("L'IBAN est requis pour les virements internationaux");
                }
                if (!IBAN_PATTERN.matcher(iban. replaceAll("\\s", ""). toUpperCase()).matches()) {
                    throw new IllegalArgumentException("Format IBAN invalide");
                }
                if (!validateIbanChecksum(iban)) {
                    throw new IllegalArgumentException("IBAN invalide (checksum incorrect)");
                }
                break;
        }
    }

    /**
     * Validate IBAN checksum (ISO 7064 Mod 97-10)
     */
    private boolean validateIbanChecksum(String iban) {
        String cleanIban = iban.replaceAll("\\s", "").toUpperCase();

        // Move first 4 characters to end
        String rearranged = cleanIban.substring(4) + cleanIban.substring(0, 4);

        // Replace letters with numbers (A=10, B=11, etc.)
        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(c - 'A' + 10);
            } else {
                numericIban. append(c);
            }
        }

        // Calculate mod 97
        try {
            java.math.BigInteger ibanNumber = new java.math.BigInteger(numericIban.toString());
            return ibanNumber.mod(java.math.BigInteger.valueOf(97)).intValue() == 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verify if account exists in same bank
     */
    private boolean verifyInternalAccount(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber). isPresent();
    }

    /**
     * Sanitize user input to prevent injection attacks
     */
    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim()
                .replaceAll("[<>\"'&]", "")
                .replaceAll("\\s+", " ");
    }
}