package com.securebank.service;

import com.securebank.model.LoginAttempt;
import com.securebank.model.User;
import com.securebank.repository.LoginAttemptRepository;
import com.securebank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

@Service
public class AccountLockService {
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Enregistre une tentative de connexion
     */
    public void recordLoginAttempt(String email, boolean successful, String failureReason, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        
        LoginAttempt attempt = new LoginAttempt(email, ipAddress, successful, failureReason);
        loginAttemptRepository.save(attempt);
        
        System.out.println(String.format("🔍 Tentative de connexion - Email: %s, IP: %s, Succès: %s", 
                                       email, ipAddress, successful));
        
        if (!successful) {
            checkAndLockAccount(email);
        }
    }
    
    /**
     * Vérifie si le compte doit être verrouillé
     */
    private void checkAndLockAccount(String email) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(email, since);
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(email);
        }
    }
    
    /**
     * Verrouille un compte utilisateur
     */
    private void lockAccount(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.isAccountNonLocked()) {
            user.setAccountNonLocked(false);
            userRepository.save(user);
            
            System.out.println("🔒 Compte verrouillé pour: " + email);
            
            // Envoyer notification de sécurité
            sendSecurityNotification(email);
        }
    }
    
    /**
     * Vérifie si un compte est verrouillé
     */
    public boolean isAccountLocked(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isAccountNonLocked()) {
            return false;
        }
        
        // Vérifier si le verrouillage a expiré
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        long recentFailedAttempts = loginAttemptRepository.countFailedAttemptsSince(email, since);
        
        if (recentFailedAttempts < MAX_FAILED_ATTEMPTS) {
            // Déverrouiller automatiquement
            unlockAccount(email);
            return false;
        }
        
        return true;
    }
    
    /**
     * Déverrouille un compte
     */
    public void unlockAccount(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && !user.isAccountNonLocked()) {
            user.setAccountNonLocked(true);
            userRepository.save(user);
            
            System.out.println("🔓 Compte déverrouillé pour: " + email);
        }
    }
    
    /**
     * Obtient le nombre de tentatives échouées récentes
     */
    public long getFailedAttemptsCount(String email) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        return loginAttemptRepository.countFailedAttemptsSince(email, since);
    }
    
    /**
     * Envoie une notification de sécurité
     */
    private void sendSecurityNotification(String email) {
        try {
            emailService.sendSecurityAlert(email);
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification: " + e.getMessage());
        }
    }
    
    /**
     * Obtient l'adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}