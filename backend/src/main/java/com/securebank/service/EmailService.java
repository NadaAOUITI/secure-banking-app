package com.securebank.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    /**
     * Envoie un email avec le code OTP
     * Pour la démo, on simule l'envoi d'email
     */
    public void sendOtpEmail(String email, String otpCode) {
        // TODO: Implémenter l'envoi d'email réel avec JavaMailSender
        // Pour l'instant, on simule avec un log
        
        System.out.println("=== EMAIL OTP ===");
        System.out.println("To: " + email);
        System.out.println("Subject: Code de vérification - Secure Banking");
        System.out.println("Body:");
        System.out.println("Votre code de vérification est: " + otpCode);
        System.out.println("Ce code expire dans 5 minutes.");
        System.out.println("Ne partagez jamais ce code avec personne.");
        System.out.println("================");
        
        // Simulation d'un délai d'envoi
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Valide le format d'email
     */
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}