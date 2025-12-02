package com.securebank. dto;

import com.securebank. model.BankCard;
import java.math.BigDecimal;
import java.time. LocalDateTime;

public class CardStatusResponse {

    private Long cardId;
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

    public static CardStatusResponse fromEntity(BankCard card) {
        card.resetDailyCountersIfNeeded();

        CardStatusResponse response = new CardStatusResponse();
        response. setCardId(card.getId());
        response.setCardType(card.getCardType().getDisplayName());
        response. setActive(card.isActive());
        response. setBlocked(card.isBlocked());
        response.setBlockReason(card.getBlockReason());
        response.setBlockedAt(card. getBlockedAt());
        response.setFailedAttempts(card.getFailedPinAttempts());
        response.setMaxAttempts(card.getMaxPinAttempts());
        response.setRemainingAttempts(card.getRemainingAttempts());
        response.setSingleTransactionLimit(card. getSingleTransactionLimit());
        response. setDailyTransactionLimit(card. getDailyTransactionLimit());
        response.setMaxDailyTransactions(card.getMaxDailyTransactions());
        response.setDailyTransactionCount(card.getDailyTransactionCount());
        response.setDailyTransactionTotal(card.getDailyTransactionTotal());
        response. setRemainingDailyLimit(
                card.getDailyTransactionLimit().subtract(card. getDailyTransactionTotal())
        );
        response.setRemainingDailyTransactions(
                card.getMaxDailyTransactions() - card. getDailyTransactionCount()
        );
        return response;
    }

    // Getters and Setters
    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

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
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public int getRemainingAttempts() { return remainingAttempts; }
    public void setRemainingAttempts(int remainingAttempts) { this.remainingAttempts = remainingAttempts; }

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

    public BigDecimal getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(BigDecimal remainingDailyLimit) { this. remainingDailyLimit = remainingDailyLimit; }

    public int getRemainingDailyTransactions() { return remainingDailyTransactions; }
    public void setRemainingDailyTransactions(int remainingDailyTransactions) { this.remainingDailyTransactions = remainingDailyTransactions; }
}