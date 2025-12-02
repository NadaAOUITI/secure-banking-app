package com.securebank. exception;

import java.math.BigDecimal;

public class TransactionLimitException extends RuntimeException {

    public enum LimitType {
        SINGLE_TRANSACTION("Limite par transaction dépassée"),
        DAILY_AMOUNT("Limite journalière dépassée"),
        DAILY_COUNT("Nombre max de transactions atteint");

        private final String message;
        LimitType(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    private final LimitType limitType;
    private final BigDecimal requested;
    private final BigDecimal limit;

    public TransactionLimitException(LimitType limitType, BigDecimal requested, BigDecimal limit) {
        super(limitType.getMessage());
        this.limitType = limitType;
        this.requested = requested;
        this.limit = limit;
    }

    public LimitType getLimitType() { return limitType; }
    public BigDecimal getRequested() { return requested; }
    public BigDecimal getLimit() { return limit; }
}