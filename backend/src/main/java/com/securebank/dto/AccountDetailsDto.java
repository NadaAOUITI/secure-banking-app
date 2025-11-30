package com.securebank.dto;

import java.math.BigDecimal;
import java.util.List;

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
        private Long id;
        private String accountNumber;
        private BigDecimal balance;
        private String accountType;
        private List<CardInfo> cards;
        
        public AccountInfo(Long id, String accountNumber, BigDecimal balance, String accountType, List<CardInfo> cards) {
            this.id = id;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.accountType = accountType;
            this.cards = cards;
        }
        
        public Long getId() { return id; }
        public String getAccountNumber() { return accountNumber; }
        public BigDecimal getBalance() { return balance; }
        public String getAccountType() { return accountType; }
        public List<CardInfo> getCards() { return cards; }
    }
    
    public static class CardInfo {
        private String maskedCardNumber;
        private String cardType;
        private String expiryDate;
        
        public CardInfo(String maskedCardNumber, String cardType, String expiryDate) {
            this.maskedCardNumber = maskedCardNumber;
            this.cardType = cardType;
            this.expiryDate = expiryDate;
        }
        
        public String getCardNumber() { return maskedCardNumber; }
        public String getCardType() { return cardType; }
        public String getExpiryDate() { return expiryDate; }
    }

    public AccountDetailsDto() {}

    public AccountDetailsDto(List<AccountInfo> accounts, String firstName, String lastName, 
                           String email, String phone, String country, String address, String currency) {
        this.accounts = accounts;
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