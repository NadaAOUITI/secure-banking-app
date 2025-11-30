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
    
    @Autowired
    private SecureDataMaskingService maskingService;

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
        
        // Construction sécurisée des AccountInfo avec masquage
        List<AccountDetailsDto.AccountInfo> accountInfos = accounts.stream()
            .map(account -> {
                List<BankCard> cards = bankCardRepository.findByAccountAndIsActiveTrue(account);
                List<AccountDetailsDto.CardInfo> cardInfos = cards.stream()
                    .map(this::createSecureCardInfo)
                    .collect(java.util.stream.Collectors.toList());
                
                return new AccountDetailsDto.AccountInfo(
                    account.getId(),
                    account.getAccountNumber(),
                    account.getBalance(),
                    account.getAccountType().name(),
                    cardInfos
                );
            })
            .collect(java.util.stream.Collectors.toList());
        
        return new AccountDetailsDto(
            accountInfos,
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            decryptedPhone,
            decryptedCountry,
            decryptedAddress,
            "TND"
        );
    }
    
    private AccountDetailsDto.CardInfo createSecureCardInfo(BankCard card) {
        String maskedNumber = maskingService.maskCardNumber(card.getCardNumberEncrypted());
        String expiryDate = maskingService.decryptSafely(card.getExpiryDateEncrypted());
        
        return new AccountDetailsDto.CardInfo(
            maskedNumber,
            card.getCardType().name(),
            expiryDate != null ? expiryDate : "**/**"
        );
    }
}