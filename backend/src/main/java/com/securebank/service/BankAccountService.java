package com.securebank.service;

import com.securebank.model.BankAccount;
import com.securebank.model.BankCard;
import com.securebank.model.User;
import com.securebank.repository.BankAccountRepository;
import com.securebank.repository.BankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class BankAccountService {
    
    private final BankAccountRepository accountRepository;
    private final BankCardRepository cardRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public BankAccountService(BankAccountRepository accountRepository, 
                             BankCardRepository cardRepository,
                             EncryptionService encryptionService,
                             PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.encryptionService = encryptionService;
        this.passwordEncoder = passwordEncoder;
    }
    
    public BankAccount createAccount(User user, String accountTypeStr, BigDecimal initialDeposit) {
        BankAccount.AccountType accountType = BankAccount.AccountType.valueOf(accountTypeStr.toUpperCase());
        
        // Validate minimum deposit if provided
        if (initialDeposit != null && initialDeposit.compareTo(new BigDecimal("300")) < 0) {
            throw new IllegalArgumentException("Le dépôt initial doit être d'au moins 300 TND");
        }
        
        BankAccount account = new BankAccount(user, accountType, initialDeposit);
        account.setAccountNumber(generateAccountNumber());
        
        return accountRepository.save(account);
    }
    
    public List<BankCard> createCards(BankAccount account, String[] cardTypes, String[] cardPins) {
        if (cardTypes == null || cardTypes.length == 0) {
            return List.of();
        }
        
        List<BankCard> cards = new java.util.ArrayList<>();
        for (int i = 0; i < cardTypes.length; i++) {
            BankCard.CardType cardType = BankCard.CardType.valueOf(cardTypes[i].toUpperCase());
            BankCard card = new BankCard(account, cardType);
            
            // Generate and encrypt card details
            card.setCardNumberEncrypted(encryptionService.encryptSensitiveData(generateCardNumber()));
            card.setExpiryDateEncrypted(encryptionService.encryptSensitiveData(generateExpiryDate()));
            card.setCvvEncrypted(encryptionService.encryptSensitiveData(generateCVV()));
            
            // Hash and set PIN if provided
            if (cardPins != null && i < cardPins.length && cardPins[i] != null) {
                card.setPinHash(passwordEncoder.encode(cardPins[i]));
            }
            
            cards.add(cardRepository.save(card));
        }
        
        return cards;
    }
    
    // Overloaded method for backward compatibility
    public List<BankCard> createCards(BankAccount account, String[] cardTypes) {
        return createCards(account, cardTypes, null);
    }
    
    public void setPinForCard(Long cardId, String pin) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));
        
        card.setPinHash(passwordEncoder.encode(pin));
        cardRepository.save(card);
    }
    
    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder accountNumber = new StringBuilder("TN59");
        
        for (int i = 0; i < 16; i++) {
            accountNumber.append(random.nextInt(10));
        }
        
        return accountNumber.toString();
    }
    
    private String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder cardNumber = new StringBuilder("4000");
        
        for (int i = 0; i < 12; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        return cardNumber.toString();
    }
    
    private String generateExpiryDate() {
        LocalDate expiry = LocalDate.now().plusYears(3);
        return expiry.format(DateTimeFormatter.ofPattern("MM/yy"));
    }
    
    private String generateCVV() {
        SecureRandom random = new SecureRandom();
        return String.format("%03d", random.nextInt(1000));
    }
}