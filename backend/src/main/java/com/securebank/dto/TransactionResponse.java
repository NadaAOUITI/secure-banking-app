package com.securebank. dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private boolean success;
    private String message;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private BigDecimal remainingDailyLimit;
    private int remainingDailyTransactions;

    public static TransactionResponse success(String refNumber, BigDecimal amount,
                                              BigDecimal remainingLimit, int remainingTx) {
        TransactionResponse response = new TransactionResponse();
        response.setSuccess(true);
        response.setMessage("Transaction effectuée avec succès");
        response.setReferenceNumber(refNumber);
        response.setAmount(amount);
        response. setTransactionDate(LocalDateTime.now());
        response.setRemainingDailyLimit(remainingLimit);
        response.setRemainingDailyTransactions(remainingTx);
        return response;
    }

    public static TransactionResponse failure(String message) {
        TransactionResponse response = new TransactionResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response. setTransactionDate(LocalDateTime.now());
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this. referenceNumber = referenceNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this. amount = amount; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(BigDecimal remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }

    public int getRemainingDailyTransactions() { return remainingDailyTransactions; }
    public void setRemainingDailyTransactions(int remainingDailyTransactions) { this. remainingDailyTransactions = remainingDailyTransactions; }
}