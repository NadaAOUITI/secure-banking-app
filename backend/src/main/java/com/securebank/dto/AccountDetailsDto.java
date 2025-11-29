package com.securebank.dto;

import com.securebank.model.BankAccount;
import com.securebank.model.BankCard;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO pour les détails du compte - expose uniquement les données nécessaires
 */
public class AccountDetailsDto {
    private List<AccountInfo> accounts;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private String address;
    private String currency;

    public static class AccountInfo {
        private String accountNumber;
        private BigDecimal balance;
        private String accountType;
        private List<CardInfo> cards;
        
        public AccountInfo(String accountNumber, BigDecimal balance, String accountType, List<CardInfo> cards) {
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.accountType = accountType;
            this.cards = cards;
        }
        
        public String getAccountNumber() { return accountNumber; }
        public BigDecimal getBalance() { return balance; }
        public String getAccountType() { return accountType; }
        public List<CardInfo> getCards() { return cards; }
    }
    
    public static class CardInfo {
        private String cardNumber;
        private String cardType;
        private String expiryDate;
        
        public CardInfo(String cardNumber, String cardType, String expiryDate) {
            this.cardNumber = cardNumber;
            this.cardType = cardType;
            this.expiryDate = expiryDate;
        }
        
        public String getCardNumber() { return cardNumber; }
        public String getCardType() { return cardType; }
        public String getExpiryDate() { return expiryDate; }
    }

    // Constructeurs
    public AccountDetailsDto() {}

    public AccountDetailsDto(List<BankAccount> bankAccounts, List<List<BankCard>> accountCards, String firstName, 
                           String lastName, String email, String phone, String country, 
                           String address, String currency) {
        this.accounts = bankAccounts.stream()
            .map(account -> {
                int accountIndex = bankAccounts.indexOf(account);
                List<BankCard> cards = accountIndex < accountCards.size() ? accountCards.get(accountIndex) : List.of();
                List<CardInfo> cardInfos = cards.stream()
                    .map(card -> new CardInfo(
                        card.getCardNumberEncrypted(), // Will be decrypted in service
                        card.getCardType().name(),
                        card.getExpiryDateEncrypted() // Will be decrypted in service
                    ))
                    .collect(Collectors.toList());
                return new AccountInfo(
                    account.getAccountNumber(),
                    account.getBalance(),
                    account.getAccountType().name(),
                    cardInfos
                );
            })
            .collect(Collectors.toList());
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.address = address;
        this.currency = currency;
    }

    // Getters et Setters
    public List<AccountInfo> getAccounts() { return accounts; }
    public void setAccounts(List<AccountInfo> accounts) { this.accounts = accounts; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }



    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}