package com.securebank.exception;

public class CardBlockedException extends RuntimeException {

    private final String reason;

    public CardBlockedException(String reason) {
        super("Carte bloquée: " + reason);
        this.reason = reason;
    }

    public String getReason() { return reason; }
}