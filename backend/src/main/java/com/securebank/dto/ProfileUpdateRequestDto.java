package com.securebank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequestDto extends SecureRequestDto {
    
    private String otp;
    
    private String currentPassword;
    
    private String newPassword;
    
    private String confirmNewPassword;
    
    private String newEmail;
    
    private String newPhone;
    
    @Size(max = 255, message = "Adresse trop longue")
    private String newAddress;
    
    @Size(max = 100, message = "Pays trop long")
    private String newCountry;

    // Getters and Setters
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
    
    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    
    public String getNewPhone() { return newPhone; }
    public void setNewPhone(String newPhone) { this.newPhone = newPhone; }
    
    public String getNewAddress() { return newAddress; }
    public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
    
    public String getNewCountry() { return newCountry; }
    public void setNewCountry(String newCountry) { this.newCountry = newCountry; }

    @Override
    public void clearSensitiveData() {
        this.otp = null;
        this.currentPassword = null;
        this.newPassword = null;
        this.confirmNewPassword = null;
    }
}