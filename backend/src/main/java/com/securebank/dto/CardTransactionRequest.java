package com.securebank.dto;

import jakarta.validation. constraints.*;
import java.math. BigDecimal;

public class CardTransactionRequest {

    @NotNull
    private Long cardId;

    @NotBlank
    @Size(min = 4, max = 6)
    @Pattern(regexp = "\\d+")
    private String pin;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String beneficiary;
    private String description;

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getBeneficiary() { return beneficiary; }
    public void setBeneficiary(String beneficiary) { this. beneficiary = beneficiary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}