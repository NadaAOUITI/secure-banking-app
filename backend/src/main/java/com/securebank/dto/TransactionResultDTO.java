package com.securebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResultDTO {

    private boolean success;
    private String message;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private BigDecimal remainingDailyLimit;
    private int remainingDailyTransactions;

    public static TransactionResultDTO success(String referenceNumber, BigDecimal amount,
                                               BigDecimal remainingDailyLimit, int remainingDailyTransactions) {
        TransactionResultDTO dto = new TransactionResultDTO();
        dto.setSuccess(true);
        dto.setMessage("Transaction effectuée avec succès");
        dto. setReferenceNumber(referenceNumber);
        dto.setAmount(amount);
        dto.setTransactionDate(LocalDateTime.now());
        dto.setRemainingDailyLimit(remainingDailyLimit);
        dto.setRemainingDailyTransactions(remainingDailyTransactions);
        return dto;
    }

    public static TransactionResultDTO failure(String message) {
        TransactionResultDTO dto = new TransactionResultDTO();
        dto.setSuccess(false);
        dto.setMessage(message);
        dto.setTransactionDate(LocalDateTime.now());
        return dto;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(BigDecimal remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }

    public int getRemainingDailyTransactions() { return remainingDailyTransactions; }
    public void setRemainingDailyTransactions(int remainingDailyTransactions) { this.remainingDailyTransactions = remainingDailyTransactions; }
}