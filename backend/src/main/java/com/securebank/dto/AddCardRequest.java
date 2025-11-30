package com.securebank.dto;

import jakarta.validation.constraints.*;

public class AddCardRequest extends SecureRequestDto {
    
    @NotNull(message = "ID du compte requis")
    @Min(value = 1, message = "ID du compte invalide")
    private Long accountId;
    
    @NotBlank(message = "Type de carte requis")
    @Pattern(regexp = "^(CLASSIC|GOLD|PLATINUM)$", message = "Type de carte invalide")
    private String cardType;
    
    @NotBlank(message = "Code PIN requis")
    @Pattern(regexp = "^\\d{4}$", message = "Code PIN doit contenir 4 chiffres")
    @Size(min = 4, max = 4, message = "Code PIN doit contenir exactement 4 chiffres")
    private String pin;
    
    @NotBlank(message = "Mot de passe requis pour cette opération")
    @Size(min = 1, max = 255, message = "Mot de passe invalide")
    private String password;
    
    public AddCardRequest() {}
    
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    @Override
    public void clearSensitiveData() {
        this.pin = clearString(this.pin);
        this.password = clearString(this.password);
    }
}