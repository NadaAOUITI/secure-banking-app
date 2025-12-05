package com.securebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    private Long id;
    private Long accountId;
    private String type;
    private BigDecimal amount;
    private String description;
    private String reference;
    private LocalDateTime date;

    // Constructeurs
    public TransactionDTO() {}

    public TransactionDTO(Long id, Long accountId, String type, BigDecimal amount,
                          String description, String reference, LocalDateTime date) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.reference = reference;
        this.date = date;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}