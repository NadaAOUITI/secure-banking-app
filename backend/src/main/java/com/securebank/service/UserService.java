package com.securebank.service;

import com.securebank.dto.UserRegistrationDto;
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
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        user.setPassword(encodedPassword);
        
        // Sauvegarder l'utilisateur
        return userRepository.save(user);
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
}