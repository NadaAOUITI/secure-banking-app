package com.securebank.service;

import com.securebank.model.OtpToken;
import com.securebank.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class OtpService {
    
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom;
    
    @Autowired
    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Génère et envoie un code OTP
     */
    public void generateAndSendOtp(String email) {
        // Nettoyer les anciens tokens
        cleanupExpiredTokens();
        
        // Marquer tous les tokens existants comme utilisés
        otpTokenRepository.markAllTokensAsUsed(email);
        
        // Générer un nouveau code OTP
        String otpCode = generateOtpCode();
        
        // Sauvegarder le token
        OtpToken otpToken = new OtpToken(email, otpCode);
        otpTokenRepository.save(otpToken);
        
        // Envoyer l'email
        emailService.sendOtpEmail(email, otpCode);
    }
    
    /**
     * Vérifie un code OTP
     */
    public boolean verifyOtp(String email, String otpCode) {
        Optional<OtpToken> tokenOpt = otpTokenRepository.findValidOtpToken(
            email, otpCode, LocalDateTime.now()
        );
        
        if (tokenOpt.isPresent()) {
            OtpToken token = tokenOpt.get();
            // Marquer le token comme utilisé
            token.setUsed(true);
            otpTokenRepository.save(token);
            return true;
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