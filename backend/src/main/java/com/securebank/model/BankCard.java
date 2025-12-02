package com. securebank.model;

import jakarta. persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java. time.LocalDateTime;

@Entity
@Table(name = "bank_cards")
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType. LAZY)
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

    // ===== NOUVEAUX CHAMPS POUR LA SÉCURITÉ =====

    @Column(name = "failed_pin_attempts")
    private int failedPinAttempts = 0;

    @Column(name = "max_pin_attempts")
    private int maxPinAttempts = 3;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "block_reason")
    private String blockReason;

    @Column(name = "single_transaction_limit")
    private BigDecimal singleTransactionLimit;

    @Column(name = "daily_transaction_limit")
    private BigDecimal dailyTransactionLimit;

    @Column(name = "max_daily_transactions")
    private int maxDailyTransactions = 10;

    @Column(name = "daily_transaction_count")
    private int dailyTransactionCount = 0;

    @Column(name = "daily_transaction_total")
    private BigDecimal dailyTransactionTotal = BigDecimal. ZERO;

    @Column(name = "last_transaction_date")
    private LocalDate lastTransactionDate;

    // ===== FIN NOUVEAUX CHAMPS =====

    public enum CardType {
        CLASSIC("Carte Classique", 0, new BigDecimal("1000.00"), new BigDecimal("3000.00"), 10),
        GOLD("Carte Gold", 150, new BigDecimal("2500.00"), new BigDecimal("7500.00"), 20),
        PLATINUM("Carte Platinum", 450, new BigDecimal("5000.00"), new BigDecimal("15000.00"), 50);

        private final String displayName;
        private final int annualFee;
        private final BigDecimal defaultSingleLimit;
        private final BigDecimal defaultDailyLimit;
        private final int defaultMaxTransactions;

        CardType(String displayName, int annualFee, BigDecimal defaultSingleLimit,
                 BigDecimal defaultDailyLimit, int defaultMaxTransactions) {
            this.displayName = displayName;
            this.annualFee = annualFee;
            this. defaultSingleLimit = defaultSingleLimit;
            this.defaultDailyLimit = defaultDailyLimit;
            this. defaultMaxTransactions = defaultMaxTransactions;
        }

        public String getDisplayName() { return displayName; }
        public int getAnnualFee() { return annualFee; }
        public BigDecimal getDefaultSingleLimit() { return defaultSingleLimit; }
        public BigDecimal getDefaultDailyLimit() { return defaultDailyLimit; }
        public int getDefaultMaxTransactions() { return defaultMaxTransactions; }
    }

    public BankCard() {
        this. createdAt = LocalDateTime.now();
    }

    public BankCard(BankAccount account, CardType cardType) {
        this();
        this. account = account;
        this.cardType = cardType;
        this.singleTransactionLimit = cardType.getDefaultSingleLimit();
        this.dailyTransactionLimit = cardType.getDefaultDailyLimit();
        this.maxDailyTransactions = cardType.getDefaultMaxTransactions();
    }

    // ===== MÉTHODES UTILITAIRES =====

    public boolean canPerformTransaction() {
        return isActive && !isBlocked;
    }

    public boolean incrementFailedAttempts() {
        this.failedPinAttempts++;
        return this.failedPinAttempts >= this.maxPinAttempts;
    }

    public void resetFailedAttempts() {
        this. failedPinAttempts = 0;
    }

    public void blockCard(String reason) {
        this.isBlocked = true;
        this.blockedAt = LocalDateTime.now();
        this.blockReason = reason;
    }

    public void unblockCard() {
        this.isBlocked = false;
        this.blockedAt = null;
        this.blockReason = null;
        this.failedPinAttempts = 0;
    }

    public void resetDailyCountersIfNeeded() {
        LocalDate today = LocalDate.now();
        if (lastTransactionDate == null || ! lastTransactionDate. equals(today)) {
            this.dailyTransactionCount = 0;
            this.dailyTransactionTotal = BigDecimal.ZERO;
            this.lastTransactionDate = today;
        }
    }

    public int getRemainingAttempts() {
        return Math.max(0, maxPinAttempts - failedPinAttempts);
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
    public void setPinHash(String pinHash) { this. pinHash = pinHash; }

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

    public int getFailedPinAttempts() { return failedPinAttempts; }
    public void setFailedPinAttempts(int failedPinAttempts) { this. failedPinAttempts = failedPinAttempts; }

    public int getMaxPinAttempts() { return maxPinAttempts; }
    public void setMaxPinAttempts(int maxPinAttempts) { this.maxPinAttempts = maxPinAttempts; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this. blockReason = blockReason; }

    public BigDecimal getSingleTransactionLimit() { return singleTransactionLimit; }
    public void setSingleTransactionLimit(BigDecimal singleTransactionLimit) { this.singleTransactionLimit = singleTransactionLimit; }

    public BigDecimal getDailyTransactionLimit() { return dailyTransactionLimit; }
    public void setDailyTransactionLimit(BigDecimal dailyTransactionLimit) { this. dailyTransactionLimit = dailyTransactionLimit; }

    public int getMaxDailyTransactions() { return maxDailyTransactions; }
    public void setMaxDailyTransactions(int maxDailyTransactions) { this. maxDailyTransactions = maxDailyTransactions; }

    public int getDailyTransactionCount() { return dailyTransactionCount; }
    public void setDailyTransactionCount(int dailyTransactionCount) { this.dailyTransactionCount = dailyTransactionCount; }

    public BigDecimal getDailyTransactionTotal() { return dailyTransactionTotal; }
    public void setDailyTransactionTotal(BigDecimal dailyTransactionTotal) { this. dailyTransactionTotal = dailyTransactionTotal; }

    public LocalDate getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDate lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }
}