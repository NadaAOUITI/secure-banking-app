package com.securebank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Email format invalide")
    @NotBlank(message = "Email requis")
    private String email;
    
    @Column(nullable = false)
    @NotBlank(message = "Prénom requis")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;
    
    @Column(nullable = false)
    @NotBlank(message = "Nom requis")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;
    
    @Column(nullable = false)
    @NotBlank(message = "Mot de passe requis")
    private String password;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(nullable = false)
    private boolean accountNonExpired = true;
    
    @Column(nullable = false)
    private boolean accountNonLocked = true;
    
    @Column(nullable = false)
    private boolean credentialsNonExpired = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastLoginAt;
    
    // Encrypted sensitive fields
    @Column(name = "country_encrypted")
    private String countryEncrypted;
    
    @Column(name = "phone_encrypted")
    private String phoneEncrypted;
    
    @Column(name = "birth_date_encrypted")
    private String birthDateEncrypted;
    
    @Column(name = "address_encrypted")
    private String addressEncrypted;
    
    @Column(name = "document_type_encrypted")
    private String documentTypeEncrypted;
    
    @Column(name = "document_number_encrypted")
    private String documentNumberEncrypted;
    
    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
    }
    
    public User(String email, String firstName, String lastName, String password) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }
    
    // Encrypted fields getters/setters
    public String getCountryEncrypted() { return countryEncrypted; }
    public void setCountryEncrypted(String countryEncrypted) { this.countryEncrypted = countryEncrypted; }
    
    public String getPhoneEncrypted() { return phoneEncrypted; }
    public void setPhoneEncrypted(String phoneEncrypted) { this.phoneEncrypted = phoneEncrypted; }
    
    public String getBirthDateEncrypted() { return birthDateEncrypted; }
    public void setBirthDateEncrypted(String birthDateEncrypted) { this.birthDateEncrypted = birthDateEncrypted; }
    
    public String getAddressEncrypted() { return addressEncrypted; }
    public void setAddressEncrypted(String addressEncrypted) { this.addressEncrypted = addressEncrypted; }
    
    public String getDocumentTypeEncrypted() { return documentTypeEncrypted; }
    public void setDocumentTypeEncrypted(String documentTypeEncrypted) { this.documentTypeEncrypted = documentTypeEncrypted; }
    
    public String getDocumentNumberEncrypted() { return documentNumberEncrypted; }
    public void setDocumentNumberEncrypted(String documentNumberEncrypted) { this.documentNumberEncrypted = documentNumberEncrypted; }
}