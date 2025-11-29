package com.securebank.dto;

public class UserDisplayDto {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneMasked;
    private String emailMasked;
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhoneMasked() {
        return phoneMasked;
    }
    
    public void setPhoneMasked(String phoneMasked) {
        this.phoneMasked = phoneMasked;
    }
    
    public String getEmailMasked() {
        return emailMasked;
    }
    
    public void setEmailMasked(String emailMasked) {
        this.emailMasked = emailMasked;
    }
}