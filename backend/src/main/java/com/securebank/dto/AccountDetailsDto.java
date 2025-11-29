package com.securebank.dto;

import java.math.BigDecimal;

/**
 * DTO pour les détails du compte - expose uniquement les données nécessaires
 */
public class AccountDetailsDto {
    private String accountNumber;
    private BigDecimal balance;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private String address;
    private String accountType;
    private String currency;

    // Constructeurs
    public AccountDetailsDto() {}

    public AccountDetailsDto(String accountNumber, BigDecimal balance, String firstName, 
                           String lastName, String email, String phone, String country, 
                           String address, String accountType, String currency) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.address = address;
        this.accountType = accountType;
        this.currency = currency;
    }

    // Getters et Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

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

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}