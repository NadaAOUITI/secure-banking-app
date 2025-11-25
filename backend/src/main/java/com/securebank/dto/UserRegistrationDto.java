package com.securebank.dto;

import com.securebank.util.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
}