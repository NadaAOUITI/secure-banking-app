package com.securebank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Envoie un email avec le code OTP
     */
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Code de vérification - Secure Banking");
            helper.setText(buildOtpEmailTemplate(otpCode), true);
            
            mailSender.send(message);
            System.out.println("✅ Email envoyé à: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur email: " + e.getMessage());
            displayOtpInConsole(toEmail, otpCode);
        }
    }
    
    /**
     * Affiche l'OTP dans la console en cas d'échec
     */
    private void displayOtpInConsole(String toEmail, String otpCode) {
        System.out.println("=== EMAIL OTP (CONSOLE) ===");
        System.out.println("To: " + toEmail);
        System.out.println("Code OTP: " + otpCode);
        System.out.println("===========================");
    }
    
    /**
     * Template HTML pour l'email OTP
     */
    private String buildOtpEmailTemplate(String otpCode) {
        return "<html>" +
               "<head><meta charset=\"UTF-8\"><title>Code de vérification</title></head>" +
               "<body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">" +
               "<div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">" +
               "<h1 style=\"color: white; margin: 0;\">🏦 Secure Banking</h1>" +
               "</div>" +
               "<div style=\"background: white; padding: 40px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">" +
               "<h2>🔐 Code de vérification</h2>" +
               "<p>Voici votre code de vérification sécurisé :</p>" +
               "<div style=\"background: #f8f9fa; border: 2px dashed #007bff; border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0;\">" +
               "<div style=\"font-size: 36px; font-weight: bold; color: #007bff; letter-spacing: 8px; font-family: monospace;\">" +
               otpCode +
               "</div>" +
               "</div>" +
               "<p><strong>⚠️ Ce code expire dans 5 minutes</strong></p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}