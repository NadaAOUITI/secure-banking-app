package com.securebank.exception;

public class InvalidPinException extends RuntimeException {

    private final int remainingAttempts;
    private final boolean cardBlocked;

    public InvalidPinException(int remainingAttempts, boolean cardBlocked) {
        super(cardBlocked
                ? "Code PIN incorrect.  Votre carte a été bloquée."
                : "Code PIN incorrect. Tentatives restantes: " + remainingAttempts);
        this. remainingAttempts = remainingAttempts;
        this.cardBlocked = cardBlocked;
    }

    public int getRemainingAttempts() { return remainingAttempts; }
    public boolean isCardBlocked() { return cardBlocked; }
}