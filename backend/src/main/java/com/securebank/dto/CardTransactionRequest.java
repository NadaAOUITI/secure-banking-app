package com.securebank. dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CardTransactionRequest {

    @NotNull(message = "ID de carte requis")
    private Long cardId;

    @NotBlank(message = "Code PIN requis")
    @Pattern(regexp = "\\d{4}", message = "Le code PIN doit contenir 4 chiffres")
    private String pin;

    @NotNull(message = "Montant requis")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")  // ✅ CORRIGÉ
    private BigDecimal amount;

    @NotBlank(message = "Type de transaction requis")
    @Pattern(regexp = "^(credit|debit)$", flags = Pattern.Flag. CASE_INSENSITIVE,
            message = "Type doit être 'credit' ou 'debit'")
    private String type;

    @NotBlank(message = "Description requise")
    @Size(max = 500, message = "Description trop longue (max 500 caractères)")
    private String description;

    @Size(max = 100, message = "Référence trop longue (max 100 caractères)")
    private String reference;

    @Size(max = 200, message = "Nom du bénéficiaire trop long")
    private String beneficiary;

    // ==================== GETTERS ====================

    public Long getCardId() {
        return cardId;
    }

    public String getPin() {
        return pin;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    // ==================== SETTERS ====================

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setAmount(BigDecimal amount) {
        this. amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    // ==================== UTILITAIRE ====================

    public void clearSensitiveData() {
        this. pin = null;
    }

    @Override
    public String toString() {
        return "CardTransactionRequest{" +
                "cardId=" + cardId +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}