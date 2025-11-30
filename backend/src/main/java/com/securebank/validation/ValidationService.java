package com.securebank.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

@Service
public class ValidationService {
    
    private final InputSanitizer inputSanitizer;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{10,20}$");
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4}$");
    
    @Autowired
    public ValidationService(InputSanitizer inputSanitizer) {
        this.inputSanitizer = inputSanitizer;
    }
    
    public ValidationResult validateAndSanitize(String input, ValidationType type) {
        if (input == null) {
            return new ValidationResult(false, "Input cannot be null", null);
        }
        
        // Check for injection attempts
        if (inputSanitizer.containsSqlInjection(input)) {
            return new ValidationResult(false, "Invalid characters detected", null);
        }
        
        if (inputSanitizer.containsXssAttempt(input)) {
            return new ValidationResult(false, "Invalid content detected", null);
        }
        
        String sanitized = inputSanitizer.sanitizeInput(input);
        
        switch (type) {
            case EMAIL:
                return validateEmail(sanitized);
            case PHONE:
                return validatePhone(sanitized);
            case ACCOUNT_NUMBER:
                return validateAccountNumber(sanitized);
            case PIN:
                return validatePin(sanitized);
            case TEXT:
                return validateText(sanitized);
            default:
                return new ValidationResult(true, "Valid", sanitized);
        }
    }
    
    private ValidationResult validateEmail(String email) {
        String sanitizedEmail = inputSanitizer.sanitizeEmail(email);
        if (!EMAIL_PATTERN.matcher(sanitizedEmail).matches()) {
            return new ValidationResult(false, "Invalid email format", null);
        }
        return new ValidationResult(true, "Valid email", sanitizedEmail);
    }
    
    private ValidationResult validatePhone(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return new ValidationResult(false, "Invalid phone format", null);
        }
        return new ValidationResult(true, "Valid phone", phone);
    }
    
    private ValidationResult validateAccountNumber(String accountNumber) {
        if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            return new ValidationResult(false, "Invalid account number format", null);
        }
        return new ValidationResult(true, "Valid account number", accountNumber);
    }
    
    private ValidationResult validatePin(String pin) {
        if (!PIN_PATTERN.matcher(pin).matches()) {
            return new ValidationResult(false, "PIN must be exactly 4 digits", null);
        }
        return new ValidationResult(true, "Valid PIN", pin);
    }
    
    private ValidationResult validateText(String text) {
        if (text.length() > 255) {
            return new ValidationResult(false, "Text too long", null);
        }
        return new ValidationResult(true, "Valid text", text);
    }
    
    public enum ValidationType {
        EMAIL, PHONE, ACCOUNT_NUMBER, PIN, TEXT
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String sanitizedValue;
        
        public ValidationResult(boolean valid, String message, String sanitizedValue) {
            this.valid = valid;
            this.message = message;
            this.sanitizedValue = sanitizedValue;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getSanitizedValue() { return sanitizedValue; }
    }
}