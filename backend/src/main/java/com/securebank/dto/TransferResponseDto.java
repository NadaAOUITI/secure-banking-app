package com.securebank.dto;

import com.securebank. model.Transfer;
import java.math.BigDecimal;
import java.time. LocalDateTime;

public class TransferResponseDto {

    private Long id;
    private String reference;
    private String senderAccountNumber;
    private String senderAccountMasked;
    private String beneficiaryName;
    private String beneficiaryAccountMasked;
    private String beneficiaryBankType;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal totalAmount;
    private String currency;
    private String description;
    private String status;
    private String statusDisplay;
    private String transferType;
    private String transferTypeDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private boolean otpRequired;

    public TransferResponseDto() {}

    public TransferResponseDto(Transfer transfer) {
        this.id = transfer. getId();
        this.reference = transfer. getReference();
        this.senderAccountNumber = transfer.getSenderAccount().getAccountNumber();
        this.senderAccountMasked = maskAccountNumber(transfer.getSenderAccount().getAccountNumber());
        this.beneficiaryName = transfer.getBeneficiary().getName();
        this.beneficiaryAccountMasked = maskAccountNumber(transfer.getBeneficiary().getAccountNumber());
        this.beneficiaryBankType = transfer. getBeneficiary(). getBankType().getDisplayName();
        this.amount = transfer.getAmount();
        this.fee = transfer.getFee();
        this.totalAmount = transfer.getTotalAmount();
        this.currency = transfer.getCurrency();
        this.description = transfer.getDescription();
        this.status = transfer.getStatus().name();
        this. statusDisplay = transfer. getStatus().getDisplayName();
        this.transferType = transfer.getTransferType().name();
        this. transferTypeDisplay = transfer.getTransferType().getDisplayName();
        this.createdAt = transfer. getCreatedAt();
        this.executedAt = transfer. getExecutedAt();
        this.otpRequired = transfer. getStatus() == Transfer.TransferStatus.OTP_REQUIRED;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return "****";
        }
        int visibleChars = 4;
        String masked = "*".repeat(accountNumber.length() - visibleChars);
        return masked + accountNumber.substring(accountNumber.length() - visibleChars);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(String senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }

    public String getSenderAccountMasked() { return senderAccountMasked; }
    public void setSenderAccountMasked(String senderAccountMasked) { this. senderAccountMasked = senderAccountMasked; }

    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }

    public String getBeneficiaryAccountMasked() { return beneficiaryAccountMasked; }
    public void setBeneficiaryAccountMasked(String beneficiaryAccountMasked) { this.beneficiaryAccountMasked = beneficiaryAccountMasked; }

    public String getBeneficiaryBankType() { return beneficiaryBankType; }
    public void setBeneficiaryBankType(String beneficiaryBankType) { this. beneficiaryBankType = beneficiaryBankType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this. currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this. description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this. status = status; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }

    public String getTransferTypeDisplay() { return transferTypeDisplay; }
    public void setTransferTypeDisplay(String transferTypeDisplay) { this.transferTypeDisplay = transferTypeDisplay; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public boolean isOtpRequired() { return otpRequired; }
    public void setOtpRequired(boolean otpRequired) { this.otpRequired = otpRequired; }
}