package com.securebank.service;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * Méthode générique pour envoyer des emails de notification
     */
    private void sendNotificationEmail(String toEmail, String subject, String title, String message, String color) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(buildNotificationTemplate(title, message, color), true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur email notification: " + e.getMessage());
        }
    }

    /**
     * Template HTML modulaire pour les notifications
     */
    private String buildNotificationTemplate(String title, String message, String color) {
        return "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<div style='background: " + color + "; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
               "<h1 style='color: white; margin: 0;'>" + title + "</h1>" +
               "</div>" +
               "<div style='background: white; padding: 40px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +
               message +
               "<p style='color: #dc3545;'><strong>Si ce n'est pas vous, contactez immédiatement notre équipe support.</strong></p>" +
               "</div></body></html>";
    }

    /**
     * Envoie un code OTP de sécurité pour modification de profil
     */
    public void sendSecurityOtp(String toEmail, String otpCode, String firstName) {
        try {
            String message = "<p>Bonjour " + firstName + ",</p>" +
                            "<p>Voici votre code de sécurité pour modifier votre profil :</p>" +
                            "<div style='background: #f8f9fa; border: 2px dashed #007bff; border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0;'>" +
                            "<div style='font-size: 36px; font-weight: bold; color: #007bff; letter-spacing: 8px; font-family: monospace;'>" +
                            otpCode + "</div></div>" +
                            "<p><strong>⚠️ Ce code expire dans 1 minute</strong></p>";
            
            sendNotificationEmail(toEmail, "🔐 Code de Sécurité - Modification Profil", "🔐 Code de Sécurité", message, "#007bff");
            System.out.println("✅ Code OTP sécurité envoyé à: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur email sécurité: " + e.getMessage());
            displayOtpInConsole(toEmail, otpCode);
        }
    }

    /**
     * Envoie une notification de changement de mot de passe
     */
    public void sendPasswordChangeNotification(String toEmail, String firstName, String clientIp) {
        String message = "<p>Bonjour " + firstName + ",</p>" +
                        "<p>Votre mot de passe a été modifié avec succès.</p>" +
                        "<p><strong>IP:</strong> " + clientIp + "</p>";
        
        sendNotificationEmail(toEmail, "🔒 Mot de passe modifié - Secure Banking", "🔒 Mot de passe modifié", message, "#28a745");
    }

    /**
     * Envoie une notification de changement d'email
     */
    public void sendEmailChangeNotification(String toEmail, String firstName, String clientIp) {
        String message = "<p>Bonjour " + firstName + ",</p>" +
                        "<p>Votre adresse email a été modifiée avec succès.</p>" +
                        "<p><strong>IP:</strong> " + clientIp + "</p>";
        
        sendNotificationEmail(toEmail, "📧 Email modifié - Secure Banking", "📧 Email Modifié", message, "#17a2b8");
    }

    /**
     * Envoie un code OTP de confirmation pour nouveau email
     */
    public void sendEmailConfirmationOtp(String toEmail, String firstName, String otpCode) {
        String message = "<p>Bonjour " + firstName + ",</p>" +
                        "<p>Voici votre code de confirmation pour votre nouvel email :</p>" +
                        "<div style='background: #f8f9fa; border: 2px dashed #28a745; border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0;'>" +
                        "<div style='font-size: 36px; font-weight: bold; color: #28a745; letter-spacing: 8px; font-family: monospace;'>" +
                        otpCode + "</div></div>" +
                        "<p><strong>⚠️ Ce code expire dans 5 minutes</strong></p>";
        
        sendNotificationEmail(toEmail, "📧 Confirmation Email - Secure Banking", "📧 Confirmez votre Email", message, "#28a745");
    }
    public void sendTransferEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("📤 Email de transfert envoyé à: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email transfert: " + e.getMessage());
        }
    }



// ==================== EMAIL DE CONFIRMATION DE TRANSACTION ====================

    /**
     * Envoie un email de confirmation de transaction
     */
    public void sendTransactionConfirmationEmail(
            String toEmail,
            String firstName,
            String lastName,
            String transactionType,
            BigDecimal amount,
            String reference,
            LocalDateTime transactionDate,
            String description,
            String cardType,
            Long cardId,
            String accountNumber,
            BigDecimal oldBalance,
            BigDecimal newBalance) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            String subject = transactionType. equalsIgnoreCase("credit")
                    ? "💰 Dépôt confirmé - SecureBank"
                    : "💸 Retrait confirmé - SecureBank";

            helper.setSubject(subject);
            helper.setText(buildTransactionEmailTemplate(
                    firstName, lastName, transactionType, amount, reference,
                    transactionDate, description, cardType, cardId, accountNumber,
                    oldBalance, newBalance
            ), true);

            mailSender.send(message);
            System.out.println("✅ Email de confirmation de transaction envoyé à: " + toEmail);

        } catch (Exception e) {
            System. err.println("❌ Erreur envoi email transaction: " + e. getMessage());
            System.out.println("=== TRANSACTION (CONSOLE) ===");
            System.out. println("To: " + toEmail + " | Type: " + transactionType + " | Montant: " + amount + " MAD | Ref: " + reference);
            System.out.println("=============================");
        }
    }

    /**
     * Template HTML pour l'email de confirmation de transaction
     */
    private String buildTransactionEmailTemplate(
            String firstName, String lastName, String transactionType, BigDecimal amount,
            String reference, LocalDateTime transactionDate, String description,
            String cardType, Long cardId, String accountNumber,
            BigDecimal oldBalance, BigDecimal newBalance) {

        boolean isCredit = transactionType.equalsIgnoreCase("credit");
        String typeLabel = isCredit ?  "Crédit (Dépôt)" : "Débit (Retrait)";
        String typeIcon = isCredit ?  "💰" : "💸";
        String amountSign = isCredit ?  "+" : "-";
        String amountColor = isCredit ?  "#4CAF50" : "#f44336";
        String headerColor = isCredit ? "#4CAF50" : "#f44336";

        String formattedDate = transactionDate. format(java.time.format.DateTimeFormatter. ofPattern("dd/MM/yyyy à HH:mm:ss"));

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background: #f5f5f5;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white;'>" +

                // Header
                "<div style='background: " + headerColor + "; color: white; padding: 25px; text-align: center;'>" +
                "<h1 style='margin: 0;'>🏦 SecureBank</h1>" +
                "<p style='margin: 5px 0 0 0;'>Confirmation de Transaction</p></div>" +

                // Content
                "<div style='padding: 25px;'>" +
                "<div style='text-align: center; margin-bottom: 20px;'>" +
                "<span style='background: #4CAF50; color: white; padding: 8px 20px; border-radius: 20px;'>✅ Transaction Réussie</span></div>" +

                "<p>Bonjour <strong>" + firstName + " " + lastName + "</strong>,</p>" +
                "<p>Votre transaction a été effectuée avec succès. </p>" +

                // Transaction details
                "<div style='background: #f8f9fa; border-left: 4px solid " + amountColor + "; border-radius: 8px; padding: 20px; margin: 20px 0;'>" +
                "<div style='color: #666;'>" + typeIcon + " " + typeLabel + "</div>" +
                "<div style='font-size: 28px; font-weight: bold; color: " + amountColor + "; margin: 10px 0;'>" + amountSign + amount. setScale(2) + " MAD</div>" +
                "<table style='width: 100%;'>" +
                "<tr><td style='padding: 8px 0; color: #666;'>Référence</td><td style='text-align: right; font-weight: 600;'>" + reference + "</td></tr>" +
                "<tr><td style='padding: 8px 0; color: #666;'>Date</td><td style='text-align: right; font-weight: 600;'>" + formattedDate + "</td></tr>" +
                "<tr><td style='padding: 8px 0; color: #666;'>Description</td><td style='text-align: right; font-weight: 600;'>" + (description != null ? description : "Transaction") + "</td></tr>" +
                "<tr><td style='padding: 8px 0; color: #666;'>Carte</td><td style='text-align: right; font-weight: 600;'>" + cardType + " (****" + cardId + ")</td></tr>" +
                "<tr><td style='padding: 8px 0; color: #666;'>Compte</td><td style='text-align: right; font-weight: 600;'>" + accountNumber + "</td></tr>" +
                "</table></div>" +

                // Balance
                "<div style='background: linear-gradient(135deg, #667eea, #764ba2); color: white; border-radius: 8px; padding: 15px; margin: 20px 0;'>" +
                "<div style='display: flex; justify-content: space-between; margin: 5px 0;'><span>Ancien solde</span><span>" + oldBalance.setScale(2) + " MAD</span></div>" +
                "<div style='display: flex; justify-content: space-between; margin: 5px 0;'><span>Transaction</span><span>" + amountSign + amount. setScale(2) + " MAD</span></div>" +
                "<div style='border-top: 1px solid rgba(255,255,255,0.3); margin-top: 10px; padding-top: 10px; display: flex; justify-content: space-between;'>" +
                "<span style='font-weight: bold;'>Nouveau solde</span><span style='font-weight: bold; font-size: 18px;'>" + newBalance.setScale(2) + " MAD</span></div></div>" +

                // Security
                "<div style='background: #fff3e0; border-left: 4px solid #ff9800; border-radius: 8px; padding: 15px; margin: 20px 0;'>" +
                "<strong style='color: #e65100;'>🔒 Sécurité</strong><br>" +
                "<span style='color: #f57c00; font-size: 13px;'>Si vous n'êtes pas à l'origine de cette transaction, contactez-nous immédiatement.</span></div>" +

                "<p>Merci de votre confiance. <br><strong>L'équipe SecureBank</strong></p></div>" +

                // Footer
                "<div style='background: #f5f5f5; padding: 15px; text-align: center; color: #666; font-size: 11px;'>" +
                "© 2024 SecureBank - Email automatique</div>" +

                "</div></body></html>";
    }


}