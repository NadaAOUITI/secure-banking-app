package com.securebank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_cards")
public class BankCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;
    
    @Column(name = "card_number_encrypted")
    private String cardNumberEncrypted;
    
    @Column(name = "pin_hash")
    private String pinHash;
    
    @Column(name = "expiry_date_encrypted")
    private String expiryDateEncrypted;
    
    @Column(name = "cvv_encrypted")
    private String cvvEncrypted;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "is_blocked")
    private boolean isBlocked = false;
    
    public enum CardType {
        CLASSIC("Carte Classique", 0),
        GOLD("Carte Gold", 150),
        PLATINUM("Carte Platinum", 450);
        
        private final String displayName;
        private final int annualFee;
        
        CardType(String displayName, int annualFee) {
            this.displayName = displayName;
            this.annualFee = annualFee;
        }
        
        public String getDisplayName() { return displayName; }
        public int getAnnualFee() { return annualFee; }
    }
    
    public BankCard() {
        this.createdAt = LocalDateTime.now();
    }
    
    public BankCard(BankAccount account, CardType cardType) {
        this();
        this.account = account;
        this.cardType = cardType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BankAccount getAccount() { return account; }
    public void setAccount(BankAccount account) { this.account = account; }
    
    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    
    public String getCardNumberEncrypted() { return cardNumberEncrypted; }
    public void setCardNumberEncrypted(String cardNumberEncrypted) { this.cardNumberEncrypted = cardNumberEncrypted; }
    
    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    
    public String getExpiryDateEncrypted() { return expiryDateEncrypted; }
    public void setExpiryDateEncrypted(String expiryDateEncrypted) { this.expiryDateEncrypted = expiryDateEncrypted; }
    
    public String getCvvEncrypted() { return cvvEncrypted; }
    public void setCvvEncrypted(String cvvEncrypted) { this.cvvEncrypted = cvvEncrypted; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
}