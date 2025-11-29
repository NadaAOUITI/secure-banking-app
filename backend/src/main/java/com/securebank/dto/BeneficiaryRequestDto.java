package com.securebank.dto;


import jakarta.validation.constraints. NotBlank;
import jakarta.validation. constraints.Pattern;
import jakarta. validation.constraints.Size;

public class BeneficiaryRequestDto {

    @NotBlank(message = "Le nom du bénéficiaire est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Le nom ne doit contenir que des lettres")
    private String name;

    @NotBlank(message = "Le numéro de compte est requis")
    @Pattern(regexp = "^[A-Z0-9]{10,34}$", message = "Format de numéro de compte invalide")
    private String accountNumber;

    @NotBlank(message = "Le type de banque est requis")
    private String bankType; // SAME_BANK, NATIONAL, INTERNATIONAL

    private String bankName;

    @Pattern(regexp = "^[A-Z0-9]{0,11}$", message = "Format de code banque invalide")
    private String bankCode;

    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$|^$", message = "Format SWIFT invalide")
    private String swiftCode;

    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$|^$", message = "Format IBAN invalide")
    private String iban;

    private String country;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankType() { return bankType; }
    public void setBankType(String bankType) { this. bankType = bankType; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getSwiftCode() { return swiftCode; }
    public void setSwiftCode(String swiftCode) { this.swiftCode = swiftCode; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}