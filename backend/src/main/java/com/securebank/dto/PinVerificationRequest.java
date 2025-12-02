package com.securebank. dto;

import jakarta.validation.constraints.*;

public class PinVerificationRequest {

    @NotNull
    private Long cardId;

    @NotBlank
    @Size(min = 4, max = 6)
    @Pattern(regexp = "\\d+")
    private String pin;

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}