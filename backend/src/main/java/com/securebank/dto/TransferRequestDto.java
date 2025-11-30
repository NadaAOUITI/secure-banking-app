package com.securebank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransferRequestDto {

    @NotNull(message = "Le compte source est requis")
    private Long senderAccountId;

    @NotNull(message = "Le bénéficiaire est requis")
    private Long beneficiaryId;

    @NotNull(message = "Le montant est requis")
    @DecimalMin(value = "1. 0", message = "Le montant minimum est 1 TND")
    @DecimalMax(value = "50000.0", message = "Le montant maximum est 50,000 TND")
    private BigDecimal amount;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s.,'-]*$", message = "Description contient des caractères invalides")
    private String description;

    private String currency = "TND";

    // For OTP verification
    private String transferReference;

    @Pattern(regexp = "^[0-9]{6}$", message = "Le code OTP doit contenir 6 chiffres")
    private String otpCode;

    // Getters and Setters
    public Long getSenderAccountId() { return senderAccountId; }
    public void setSenderAccountId(Long senderAccountId) { this.senderAccountId = senderAccountId; }

    public Long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(Long beneficiaryId) { this.beneficiaryId = beneficiaryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTransferReference() { return transferReference; }
    public void setTransferReference(String transferReference) { this.transferReference = transferReference; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this. otpCode = otpCode; }
}