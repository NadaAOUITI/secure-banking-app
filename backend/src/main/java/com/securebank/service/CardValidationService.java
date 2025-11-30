package com.securebank.service;

import com.securebank.model.BankAccount;
import com.securebank.model.BankCard;
import com.securebank.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class CardValidationService {
    
    private static final int MAX_CARDS_PER_ACCOUNT = 3;
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern SEQUENTIAL_PATTERN = Pattern.compile("(.)\\1{3}|0123|1234|2345|3456|4567|5678|6789|9876|8765|7654|6543|5432|4321|3210");
    
    private final BankAccountRepository bankAccountRepository;
    
    @Autowired
    public CardValidationService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }
    
    public ValidationResult validateCardAddition(Long accountId, String cardType, String pin) {
        // Validate account exists and get card count
        BankAccount account = bankAccountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return new ValidationResult(false, "Compte inexistant");
        }
        
        // Check maximum cards per account
        long activeCardCount = account.getUser().getId() != null ? 
            bankAccountRepository.countActiveCardsByAccountId(accountId) : 0;
        
        if (activeCardCount >= MAX_CARDS_PER_ACCOUNT) {
            return new ValidationResult(false, "Nombre maximum de cartes atteint pour ce compte");
        }
        
        // Validate card type
        if (!isValidCardType(cardType)) {
            return new ValidationResult(false, "Type de carte invalide");
        }
        
        // Validate PIN security
        ValidationResult pinValidation = validatePinSecurity(pin);
        if (!pinValidation.isValid()) {
            return pinValidation;
        }
        
        return new ValidationResult(true, "Validation réussie");
    }
    
    private boolean isValidCardType(String cardType) {
        return cardType != null && 
               (cardType.equals("CLASSIC") || cardType.equals("GOLD") || cardType.equals("PLATINUM"));
    }
    
    private ValidationResult validatePinSecurity(String pin) {
        if (pin == null || pin.isEmpty()) {
            return new ValidationResult(false, "Code PIN requis");
        }
        
        if (!PIN_PATTERN.matcher(pin).matches()) {
            return new ValidationResult(false, "Code PIN doit contenir exactement 4 chiffres");
        }
        
        // Check for weak PINs
        if (SEQUENTIAL_PATTERN.matcher(pin).find()) {
            return new ValidationResult(false, "Code PIN trop faible (séquence détectée)");
        }
        
        // Check for repeated digits
        if (pin.equals("0000") || pin.equals("1111") || pin.equals("2222") || 
            pin.equals("3333") || pin.equals("4444") || pin.equals("5555") || 
            pin.equals("6666") || pin.equals("7777") || pin.equals("8888") || 
            pin.equals("9999")) {
            return new ValidationResult(false, "Code PIN trop faible (chiffres répétés)");
        }
        
        return new ValidationResult(true, "Code PIN valide");
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}