package com.securebank.factory;

import com.securebank.model.BankAccount;
import com.securebank.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;

/**
 * Factory Pattern pour création de comptes bancaires
 * Préserve exactement la logique métier existante
 */
@Component
public class BankAccountFactory {
    
    public BankAccount createAccount(User user, String accountTypeStr, BigDecimal initialDeposit) {
        BankAccount.AccountType accountType = BankAccount.AccountType.valueOf(accountTypeStr.toUpperCase());
        
        // Validation identique à l'originale
        if (initialDeposit != null && initialDeposit.compareTo(new BigDecimal("300")) < 0) {
            throw new IllegalArgumentException("Le dépôt initial doit être d'au moins 300 TND");
        }
        
        BankAccount account = new BankAccount(user, accountType, initialDeposit);
        account.setAccountNumber(generateAccountNumber());
        
        return account;
    }
    
    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder accountNumber = new StringBuilder("TN59");
        
        for (int i = 0; i < 16; i++) {
            accountNumber.append(random.nextInt(10));
        }
        
        return accountNumber.toString();
    }
}