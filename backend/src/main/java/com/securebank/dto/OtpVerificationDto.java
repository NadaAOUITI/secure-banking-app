package com.securebank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerificationDto {
    
    @NotBlank(message = "Email requis")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Code OTP requis")
    @Pattern(regexp = "\\d{6}", message = "Le code OTP doit contenir 6 chiffres")
    private String otpCode;
    
    // Constructors
    public OtpVerificationDto() {}
    
    public OtpVerificationDto(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }
    
    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}