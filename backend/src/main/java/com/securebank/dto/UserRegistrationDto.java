package com.securebank.dto;

import com.securebank.util.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

public class UserRegistrationDto {
    
    @NotBlank(message = "Email requis")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Prénom requis")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;
    
    @NotBlank(message = "Nom requis")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;
    
    @NotBlank(message = "Mot de passe requis")
    @ValidPassword
    private String password;
    
    @NotBlank(message = "Confirmation du mot de passe requise")
    private String confirmPassword;
    
    // Personal Information
    @NotBlank(message = "Pays requis")
    private String country;
    
    @NotBlank(message = "Téléphone requis")
    private String phone;
    
    @NotBlank(message = "Date de naissance requise")
    private String birthDate;
    
    // Address Information
    @NotBlank(message = "Adresse requise")
    private String address;
    
    @NotBlank(message = "Ville requise")
    private String city;
    
    @NotBlank(message = "Code postal requis")
    private String postalCode;
    
    // Identity Document
    @NotBlank(message = "Type de document requis")
    private String documentType;
    
    @NotBlank(message = "Numéro de document requis")
    private String documentNumber;
    
    // Account Selection
    @NotBlank(message = "Type de compte requis")
    private String accountType;
    
    private String[] selectedCards;
    
    @Pattern(regexp = "^$|^[0-9]+(\\.[0-9]+)?$", message = "Le montant doit être un nombre valide")
    private String initialDeposit;
    
    // Constructors
    public UserRegistrationDto() {}
    
    public UserRegistrationDto(String email, String firstName, String lastName, String password, String confirmPassword) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
    
    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    
    // New fields getters/setters
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    
    public String[] getSelectedCards() { return selectedCards; }
    public void setSelectedCards(String[] selectedCards) { this.selectedCards = selectedCards; }
    
    public String getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(String initialDeposit) { this.initialDeposit = initialDeposit; }
    
    // PIN codes for cards
    private String[] cardPins;
    
    public String[] getCardPins() { return cardPins; }
    public void setCardPins(String[] cardPins) { this.cardPins = cardPins; }
}