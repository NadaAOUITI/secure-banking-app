package com.securebank.dto;

import com.securebank.model.Beneficiary;
import java.time.LocalDateTime;

public class BeneficiaryResponseDto {

    private Long id;
    private String name;
    private String accountNumber;
    private String maskedAccountNumber;
    private String bankType;
    private String bankTypeDisplay;
    private String bankName;
    private String bankCode;
    private String country;
    private boolean isVerified;
    private LocalDateTime createdAt;

    public BeneficiaryResponseDto() {}

    public BeneficiaryResponseDto(Beneficiary beneficiary) {
        this.id = beneficiary.getId();
        this. name = beneficiary. getName();
        this.accountNumber = beneficiary.getAccountNumber();
        this. maskedAccountNumber = maskAccountNumber(beneficiary.getAccountNumber());
        this.bankType = beneficiary. getBankType().name();
        this. bankTypeDisplay = beneficiary.getBankType().getDisplayName();
        this.bankName = beneficiary. getBankName();
        this.bankCode = beneficiary.getBankCode();
        this.country = beneficiary.getCountry();
        this. isVerified = beneficiary.isVerified();
        this.createdAt = beneficiary.getCreatedAt();
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getMaskedAccountNumber() { return maskedAccountNumber; }
    public void setMaskedAccountNumber(String maskedAccountNumber) { this. maskedAccountNumber = maskedAccountNumber; }

    public String getBankType() { return bankType; }
    public void setBankType(String bankType) { this.bankType = bankType; }

    public String getBankTypeDisplay() { return bankTypeDisplay; }
    public void setBankTypeDisplay(String bankTypeDisplay) { this.bankTypeDisplay = bankTypeDisplay; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}