package com.securebank.service;

import com.securebank.dto.AccountDetailsDto;
import com.securebank.model.User;
import com.securebank.model.BankAccount;
import com.securebank.repository.UserRepository;
import com.securebank.repository.BankAccountRepository;
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

        // Récupération du compte bancaire principal
        List<BankAccount> accounts = bankAccountRepository.findByUserAndIsActiveTrue(user);
        BankAccount primaryAccount = accounts.isEmpty() ? null : accounts.get(0);
        
        // Construction du DTO avec les données déchiffrées
        return new AccountDetailsDto(
            primaryAccount != null ? primaryAccount.getAccountNumber() : "Aucun compte",
            primaryAccount != null ? primaryAccount.getBalance() : java.math.BigDecimal.ZERO,
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            decryptedPhone,
            decryptedCountry,
            decryptedAddress,
            primaryAccount != null ? primaryAccount.getAccountType().name() : "AUCUN",
            "TND"
        );
    }
}