package com.securebank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints. NotBlank;
import jakarta.validation. constraints.Size;
import java. time.LocalDateTime;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType. LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @NotBlank(message = "Nom du bénéficiaire requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Column(name = "account_number", nullable = false)
    @NotBlank(message = "Numéro de compte requis")
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_type", nullable = false)
    private BankType bankType;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "iban")
    private String iban;

    @Column(name = "country")
    private String country;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BankType {
        SAME_BANK("Même Banque"),
        NATIONAL("Banque Nationale"),
        INTERNATIONAL("Banque Internationale");

        private final String displayName;

        BankType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Beneficiary() {
        this.createdAt = LocalDateTime.now();
    }

    public Beneficiary(User user, String name, String accountNumber, BankType bankType) {
        this();
        this.user = user;
        this.name = name;
        this. accountNumber = accountNumber;
        this. bankType = bankType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this. accountNumber = accountNumber; }

    public BankType getBankType() { return bankType; }
    public void setBankType(BankType bankType) { this.bankType = bankType; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this. bankName = bankName; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getSwiftCode() { return swiftCode; }
    public void setSwiftCode(String swiftCode) { this. swiftCode = swiftCode; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this. iban = iban; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}