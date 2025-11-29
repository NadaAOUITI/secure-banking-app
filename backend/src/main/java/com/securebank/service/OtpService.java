package com.securebank.service;

import com.securebank.model.OtpToken;
import com.securebank.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OtpService {
    
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    
    @Autowired
    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Génère et envoie un code OTP (login - 5 minutes)
     */
    public void generateAndSendOtp(String email) {
        generateAndSendOtp(email, 5, "login");
    }
    
    /**
     * Génère et envoie un code OTP pour onboarding (1 minute)
     */
    public void generateAndSendOnboardingOtp(String email) {
        generateAndSendOtp(email, 1, "onboarding");
    }
    
    /**
     * Génère et envoie un code OTP avec durée personnalisée
     */
    private void generateAndSendOtp(String email, int expiryMinutes, String context) {
        // Nettoyer les anciens tokens
        cleanupExpiredTokens();
        
        // Marquer tous les tokens existants comme utilisés
        otpTokenRepository.markAllTokensAsUsed(email);
        
        // Générer un nouveau code OTP
        String otpCode = generateOtpCode();
        
        // Hacher le code OTP avant stockage
        String otpCodeHash = passwordEncoder.encode(otpCode);
        
        // Sauvegarder le token avec durée personnalisée
        OtpToken otpToken = new OtpToken(email, otpCodeHash, expiryMinutes);
        otpTokenRepository.save(otpToken);
        
        // Envoyer l'email avec le bon message
        if ("onboarding".equals(context)) {
            emailService.sendOnboardingOtpEmail(email, otpCode, expiryMinutes);
        } else {
            emailService.sendOtpEmail(email, otpCode);
        }
    }
    
    /**
     * Vérifie un code OTP
     */
    public boolean verifyOtp(String email, String otpCode) {
        // Récupérer tous les tokens valides pour cet email
        List<OtpToken> validTokens = otpTokenRepository.findValidOtpTokensByEmail(email, LocalDateTime.now());
        
        for (OtpToken token : validTokens) {
            // Vérifier si le code correspond au hash
            if (passwordEncoder.matches(otpCode, token.getOtpCodeHash())) {
                // Marquer le token comme utilisé
                token.setUsed(true);
                otpTokenRepository.save(token);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Génère un code OTP à 6 chiffres
     */
    private String generateOtpCode() {
        int otp = 100000 + secureRandom.nextInt(900000); // 6 chiffres
        return String.valueOf(otp);
    }
    
    /**
     * Nettoie les tokens expirés
     */
    private void cleanupExpiredTokens() {
        otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}