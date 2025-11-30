package com.securebank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AddAccountRequest extends SecureRequestDto {
    
    @NotBlank(message = "Le type de compte est requis")
    @Pattern(regexp = "^(CURRENT|SAVINGS|PREMIUM)$", message = "Type de compte invalide")
    private String accountType;
    
    @DecimalMin(value = "300.0", message = "Le dépôt initial minimum est de 300 TND")
    @DecimalMax(value = "1000000.0", message = "Le dépôt initial maximum est de 1,000,000 TND")
    @Digits(integer = 10, fraction = 3, message = "Format de montant invalide")
    private BigDecimal initialDeposit;
    
    private String[] selectedCards;
    private String[] cardPins;
    
    @NotBlank(message = "Mot de passe requis pour cette opération")
    private String password;
    
    public AddAccountRequest() {}
    
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    
    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }
    
    public String[] getSelectedCards() { return selectedCards; }
    public void setSelectedCards(String[] selectedCards) { this.selectedCards = selectedCards; }
    
    public String[] getCardPins() { return cardPins; }
    public void setCardPins(String[] cardPins) { this.cardPins = cardPins; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    @Override
    public void clearSensitiveData() {
        clearStringArray(this.cardPins);
        this.password = clearString(this.password);
    }
}