package com.securebank.service;

import com.securebank.dto.AccountDetailsDto;
import com.securebank.model.User;
import com.securebank.model.BankAccount;
import com.securebank.model.BankCard;
import com.securebank.repository.UserRepository;
import com.securebank.repository.BankAccountRepository;
import com.securebank.repository.BankCardRepository;
import com.securebank.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service pour la gestion des comptes bancaires
 */
@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BankAccountRepository bankAccountRepository;
    
    @Autowired
    private BankCardRepository bankCardRepository;
    
    @Autowired
    private EncryptionUtil encryptionUtil;

    /**
     * Récupère les détails du compte de l'utilisateur authentifié
     */
    public AccountDetailsDto getAccountDetails() {
        // Récupération de l'utilisateur authentifié
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Déchiffrement des données sensibles
        String decryptedPhone = user.getPhoneEncrypted() != null ? encryptionUtil.decrypt(user.getPhoneEncrypted()) : null;
        String decryptedCountry = user.getCountryEncrypted() != null ? encryptionUtil.decrypt(user.getCountryEncrypted()) : null;
        String decryptedAddress = user.getAddressEncrypted() != null ? encryptionUtil.decrypt(user.getAddressEncrypted()) : null;

        // Récupération de tous les comptes bancaires actifs
        List<BankAccount> accounts = bankAccountRepository.findByUserAndIsActiveTrue(user);
        
        // Récupération des cartes pour chaque compte
        List<List<BankCard>> accountCards = accounts.stream()
            .map(account -> {
                List<BankCard> cards = bankCardRepository.findByAccountAndIsActiveTrue(account);
                // Déchiffrer les données des cartes
                cards.forEach(card -> {
                    if (card.getCardNumberEncrypted() != null) {
                        card.setCardNumberEncrypted(encryptionUtil.decrypt(card.getCardNumberEncrypted()));
                    }
                    if (card.getExpiryDateEncrypted() != null) {
                        card.setExpiryDateEncrypted(encryptionUtil.decrypt(card.getExpiryDateEncrypted()));
                    }
                });
                return cards;
            })
            .collect(java.util.stream.Collectors.toList());
        
        // Construction du DTO avec tous les comptes et cartes
        return new AccountDetailsDto(
            accounts,
            accountCards,
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            decryptedPhone,
            decryptedCountry,
            decryptedAddress,
            "TND"
        );
    }
}