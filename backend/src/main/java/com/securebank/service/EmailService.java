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
     * Envoie un email avec le code OTP (login - 5 minutes)
     */
    public void sendOtpEmail(String toEmail, String otpCode) {
        sendOtpEmail(toEmail, otpCode, 5, "login");
    }
    
    /**
     * Envoie un email avec le code OTP pour onboarding
     */
    public void sendOnboardingOtpEmail(String toEmail, String otpCode, int expiryMinutes) {
        sendOtpEmail(toEmail, otpCode, expiryMinutes, "onboarding");
    }
    
    /**
     * Envoie un email avec le code OTP avec durée personnalisée
     */
    private void sendOtpEmail(String toEmail, String otpCode, int expiryMinutes, String context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            if ("onboarding".equals(context)) {
                helper.setSubject("🔐 Vérification Email - Création de Compte");
                helper.setText(buildOnboardingOtpEmailTemplate(otpCode, expiryMinutes), true);
            } else {
                helper.setSubject("🔐 Code de vérification - Secure Banking");
                helper.setText(buildOtpEmailTemplate(otpCode), true);
            }
            
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
     * Template HTML pour l'email OTP onboarding
     */
    private String buildOnboardingOtpEmailTemplate(String otpCode, int expiryMinutes) {
        String expiryText = expiryMinutes == 1 ? "1 minute" : expiryMinutes + " minutes";
        
        return "<html>" +
               "<head><meta charset=\"UTF-8\"><title>Vérification Email</title></head>" +
               "<body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">" +
               "<div style=\"background: linear-gradient(135deg, #90EE90 0%, #006400 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">" +
               "<h1 style=\"color: white; margin: 0;\">🏦 Secure Banking</h1>" +
               "<p style=\"color: #f0fff0; margin: 10px 0 0 0;\">Création de votre compte</p>" +
               "</div>" +
               "<div style=\"background: white; padding: 40px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">" +
               "<h2>📧 Vérification de votre email</h2>" +
               "<p>Pour finaliser la création de votre compte, veuillez saisir ce code :</p>" +
               "<div style=\"background: #f0fff0; border: 2px dashed #006400; border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0;\">" +
               "<div style=\"font-size: 36px; font-weight: bold; color: #006400; letter-spacing: 8px; font-family: monospace;\">" +
               otpCode +
               "</div>" +
               "</div>" +
               "<p><strong>⚠️ Ce code expire dans " + expiryText + "</strong></p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Template HTML pour l'email OTP login
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
    
    /**
     * Envoie une alerte de sécurité pour compte verrouillé
     */
    public void sendSecurityAlert(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🚨 Alerte Sécurité - Compte Verrouillé - Secure Banking");
            helper.setText(buildSecurityAlertTemplate(), true);
            
            mailSender.send(message);
            System.out.println("✅ Alerte de sécurité envoyée à: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi alerte sécurité: " + e.getMessage());
        }
    }
    
    /**
     * Template HTML pour l'alerte de sécurité
     */
    private String buildSecurityAlertTemplate() {
        return "<html>" +
               "<head><meta charset=\"UTF-8\"><title>Alerte Sécurité</title></head>" +
               "<body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">" +
               "<div style=\"background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">" +
               "<h1 style=\"color: white; margin: 0;\">🚨 ALERTE SÉCURITÉ</h1>" +
               "<p style=\"color: #f8f9fa; margin: 10px 0 0 0;\">Secure Banking</p>" +
               "</div>" +
               "<div style=\"background: white; padding: 40px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">" +
               "<h2 style=\"color: #dc3545;\">🔒 Compte Temporairement Verrouillé</h2>" +
               "<p>Votre compte a été automatiquement verrouillé après <strong>5 tentatives de connexion échouées</strong> consécutives.</p>" +
               "<div style=\"background: #f8d7da; border: 1px solid #f5c6cb; border-radius: 8px; padding: 20px; margin: 20px 0;\">" +
               "<h3 style=\"color: #721c24; margin-top: 0;\">📈 Détails de Sécurité</h3>" +
               "<ul style=\"color: #721c24;\">" +
               "<li><strong>Tentatives échouées :</strong> 5/5</li>" +
               "<li><strong>Durée du verrouillage :</strong> 30 minutes</li>" +
               "<li><strong>Déverrouillage automatique :</strong> Dans 30 minutes</li>" +
               "</ul>" +
               "</div>" +
               "<div style=\"background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin: 20px 0;\">" +
               "<h3 style=\"color: #856404; margin-top: 0;\">🔒 Mesures de Sécurité Activées</h3>" +
               "<ul style=\"color: #856404;\">" +
               "<li>✅ Compte temporairement inaccessible</li>" +
               "<li>✅ Toutes les sessions actives terminées</li>" +
               "<li>✅ Notification envoyée à cette adresse</li>" +
               "</ul>" +
               "</div>" +
               "<div style=\"background: #d1ecf1; border: 1px solid #bee5eb; border-radius: 8px; padding: 20px; margin: 20px 0;\">" +
               "<h3 style=\"color: #0c5460; margin-top: 0;\">🛡️ Si ce n'était pas vous</h3>" +
               "<ul style=\"color: #0c5460;\">" +
               "<li>Changez votre mot de passe immédiatement après déverrouillage</li>" +
               "<li>Vérifiez vos autres comptes</li>" +
               "<li>Contactez notre support : support@securebanking.com</li>" +
               "</ul>" +
               "</div>" +
               "<div style=\"background: #e7f3ff; border-left: 4px solid #007bff; padding: 15px; margin: 20px 0;\">" +
               "<h4 style=\"color: #004085; margin-top: 0;\">💡 Conseils de Sécurité</h4>" +
               "<ul style=\"color: #004085;\">" +
               "<li>Utilisez un mot de passe fort et unique</li>" +
               "<li>Activez l'authentification à deux facteurs</li>" +
               "<li>Ne partagez jamais vos identifiants</li>" +
               "</ul>" +
               "</div>" +
               "<hr style=\"border: none; border-top: 1px solid #eee; margin: 30px 0;\">" +
               "<p style=\"color: #666; font-size: 12px; text-align: center;\">" +
               "Cet email a été envoyé automatiquement par Secure Banking<br>" +
               "© 2024 Secure Banking. Tous droits réservés." +
               "</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}