package com.securebank.service;

import com.securebank.dto.UserRegistrationDto;
import com.securebank.dto.UserDisplayDto;
import com.securebank.exception.UserAlreadyExistsException;
import com.securebank.exception.PasswordMismatchException;
import com.securebank.model.User;
import com.securebank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountLockService accountLockService;
    private final EncryptionService encryptionService;
    private final BankAccountService bankAccountService;
    private final SecureDataMaskingService maskingService;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      AccountLockService accountLockService, EncryptionService encryptionService,
                      BankAccountService bankAccountService, SecureDataMaskingService maskingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountLockService = accountLockService;
        this.encryptionService = encryptionService;
        this.bankAccountService = bankAccountService;
        this.maskingService = maskingService;
    }
    
    /**
     * Enregistre un nouvel utilisateur
     * @param registrationDto les données d'inscription
     * @return l'utilisateur créé
     * @throws UserAlreadyExistsException si l'email existe déjà
     * @throws PasswordMismatchException si les mots de passe ne correspondent pas
     */
    public User registerUser(UserRegistrationDto registrationDto) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Un compte avec cet email existe déjà");
        }
        
        // Vérifier que les mots de passe correspondent
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new PasswordMismatchException("Les mots de passe ne correspondent pas");
        }
        
        // Créer le nouvel utilisateur
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        
        // Chiffrer le mot de passe
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        
        // Chiffrer les données sensibles
        user.setCountryEncrypted(encryptionService.encryptSensitiveData(registrationDto.getCountry()));
        user.setPhoneEncrypted(encryptionService.encryptSensitiveData(registrationDto.getPhone()));
        user.setBirthDateEncrypted(encryptionService.encryptSensitiveData(registrationDto.getBirthDate()));
        
        // Combiner adresse complète (adresse + ville + code postal)
        String fullAddress = registrationDto.getAddress() + ", " + registrationDto.getCity() + " " + registrationDto.getPostalCode();
        user.setAddressEncrypted(encryptionService.encryptSensitiveData(fullAddress));
        
        user.setDocumentTypeEncrypted(encryptionService.encryptSensitiveData(registrationDto.getDocumentType()));
        user.setDocumentNumberEncrypted(encryptionService.encryptSensitiveData(registrationDto.getDocumentNumber()));
        
        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);
        
        // Créer le compte bancaire
        createBankingProducts(savedUser, registrationDto);
        
        return savedUser;
    }
    
    /**
     * Trouve un utilisateur par son email
     * @param email l'email de l'utilisateur
     * @return l'utilisateur s'il existe
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Vérifie si un email existe
     * @param email l'email à vérifier
     * @return true si l'email existe
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Authentifie un utilisateur avec email et mot de passe
     * @param email l'email de l'utilisateur
     * @param password le mot de passe
     * @return l'utilisateur si authentification réussie, null sinon
     */
    public User authenticateUser(String email, String password) {
        // Vérifier si le compte est verrouillé
        if (accountLockService.isAccountLocked(email)) {
            return null; // Compte verrouillé
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Vérifier le mot de passe
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Mettre à jour la dernière connexion
                user.setLastLoginAt(LocalDateTime.now());
                return userRepository.save(user);
            }
        }
        
        return null;
    }
    
    /**
     * Vérifie si un compte est verrouillé
     */
    public boolean isAccountLocked(String email) {
        return accountLockService.isAccountLocked(email);
    }
    
    /**
     * Obtient le nombre de tentatives échouées
     */
    public long getFailedAttemptsCount(String email) {
        return accountLockService.getFailedAttemptsCount(email);
    }
    
    /**
     * Crée les produits bancaires pour l'utilisateur
     */
    private void createBankingProducts(User user, UserRegistrationDto registrationDto) {
        try {
            // Créer le compte principal
            java.math.BigDecimal initialDeposit = null;
            if (registrationDto.getInitialDeposit() != null && !registrationDto.getInitialDeposit().isEmpty()) {
                initialDeposit = new java.math.BigDecimal(registrationDto.getInitialDeposit());
            }
            
            var account = bankAccountService.createAccount(user, registrationDto.getAccountType(), initialDeposit);
            
            // Créer les cartes sélectionnées avec leurs codes PIN
            if (registrationDto.getSelectedCards() != null && registrationDto.getSelectedCards().length > 0) {
                bankAccountService.createCards(account, registrationDto.getSelectedCards(), registrationDto.getCardPins());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création des produits bancaires", e);
        }
    }
    
    /**
     * Obtient les données utilisateur déchiffrées pour affichage sécurisé
     */
    public UserDisplayDto getUserDisplayData(String email) {
        User user = findByEmail(email);
        if (user == null) return null;
        
        UserDisplayDto dto = new UserDisplayDto();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        
        // Données masquées pour sécurité
        dto.setPhoneMasked(maskingService.maskPhone(user.getPhoneEncrypted()));
        dto.setEmailMasked(maskingService.maskEmail(user.getEmail()));
        
        return dto;
    }
}