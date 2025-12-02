package com.securebank. dto;


import com.securebank.model.BankCard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CardStatusDTO {

    private String maskedCardNumber;
    private String cardType;
    private boolean isActive;
    private boolean isBlocked;
    private String blockReason;
    private LocalDateTime blockedAt;
    private int failedAttempts;
    private int maxAttempts;
    private int remainingAttempts;
    private BigDecimal singleTransactionLimit;
    private BigDecimal dailyTransactionLimit;
    private int maxDailyTransactions;
    private int dailyTransactionCount;
    private BigDecimal dailyTransactionTotal;
    private BigDecimal remainingDailyLimit;
    private int remainingDailyTransactions;

    public static CardStatusDTO fromEntity(BankCard card, String decryptedCardNumber) {
        CardStatusDTO dto = new CardStatusDTO();
        dto.setMaskedCardNumber(maskCardNumber(decryptedCardNumber));
        dto.setCardType(card.getCardType().getDisplayName());
        dto.setActive(card.isActive());
        dto. setBlocked(card.isBlocked());
        dto.setBlockReason(card.getBlockReason());
        dto.setBlockedAt(card. getBlockedAt());
        dto.setFailedAttempts(card.getFailedPinAttempts());
        dto.setMaxAttempts(card.getMaxPinAttempts());
        dto.setRemainingAttempts(card.getRemainingAttempts());
        dto.setSingleTransactionLimit(card. getSingleTransactionLimit());
        dto. setDailyTransactionLimit(card. getDailyTransactionLimit());
        dto.setMaxDailyTransactions(card.getMaxDailyTransactions());
        dto.setDailyTransactionCount(card.getDailyTransactionCount());
        dto.setDailyTransactionTotal(card.getDailyTransactionTotal());
        dto. setRemainingDailyLimit(card.getDailyTransactionLimit(). subtract(card.getDailyTransactionTotal()));
        dto. setRemainingDailyTransactions(card.getMaxDailyTransactions() - card.getDailyTransactionCount());
        return dto;
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber. substring(cardNumber. length() - 4);
    }

    // Getters and Setters
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this. maskedCardNumber = maskedCardNumber; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this. failedAttempts = failedAttempts; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public int getRemainingAttempts() { return remainingAttempts; }
    public void setRemainingAttempts(int remainingAttempts) { this.remainingAttempts = remainingAttempts; }

    public BigDecimal getSingleTransactionLimit() { return singleTransactionLimit; }
    public void setSingleTransactionLimit(BigDecimal singleTransactionLimit) { this. singleTransactionLimit = singleTransactionLimit; }

    public BigDecimal getDailyTransactionLimit() { return dailyTransactionLimit; }
    public void setDailyTransactionLimit(BigDecimal dailyTransactionLimit) { this.dailyTransactionLimit = dailyTransactionLimit; }

    public int getMaxDailyTransactions() { return maxDailyTransactions; }
    public void setMaxDailyTransactions(int maxDailyTransactions) { this.maxDailyTransactions = maxDailyTransactions; }

    public int getDailyTransactionCount() { return dailyTransactionCount; }
    public void setDailyTransactionCount(int dailyTransactionCount) { this. dailyTransactionCount = dailyTransactionCount; }

    public BigDecimal getDailyTransactionTotal() { return dailyTransactionTotal; }
    public void setDailyTransactionTotal(BigDecimal dailyTransactionTotal) { this.dailyTransactionTotal = dailyTransactionTotal; }

    public BigDecimal getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(BigDecimal remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }

    public int getRemainingDailyTransactions() { return remainingDailyTransactions; }
    public void setRemainingDailyTransactions(int remainingDailyTransactions) { this.remainingDailyTransactions = remainingDailyTransactions; }
}