package com.securebank.service;

import com.securebank.dto.ProfileUpdateRequestDto;
import com.securebank.model.User;
import com.securebank.model.OtpToken;
import com.securebank.repository.UserRepository;
import com.securebank.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class ProfileUpdateService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final EmailService emailService;
    private final SecurityAuditService auditService;
    private final SessionService sessionService;

    @Autowired
    public ProfileUpdateService(UserRepository userRepository,
                              OtpTokenRepository otpTokenRepository,
                              PasswordEncoder passwordEncoder,
                              EncryptionService encryptionService,
                              EmailService emailService,
                              SecurityAuditService auditService,
                              SessionService sessionService) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
        this.emailService = emailService;
        this.auditService = auditService;
        this.sessionService = sessionService;
    }

    public Map<String, Object> initiateProfileUpdate(String userEmail, String clientIp) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            String otpCode = generateSecureOtp();
            String otpCodeHash = passwordEncoder.encode(otpCode);
            OtpToken otpToken = new OtpToken(user.getEmail(), otpCodeHash, 1);
            otpTokenRepository.save(otpToken);
            
            emailService.sendSecurityOtp(user.getEmail(), otpCode, user.getFirstName());
            auditService.logProfileUpdateInitiation(userEmail, clientIp);
            
            response.put("success", true);
            response.put("message", "Code de sécurité envoyé par email");
            response.put("expiresIn", 60);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi du code");
        }
        
        return response;
    }

    public Map<String, Object> updateProfile(ProfileUpdateRequestDto request, String userEmail, String clientIp, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            if (isPasswordChange(request)) {
                return handlePasswordChange(request, user, clientIp, httpRequest);
            } else if (isEmailChange(request)) {
                return handleEmailChange(request, user, clientIp);
            } else if (isPhoneChange(request)) {
                return handlePhoneChange(request, user, clientIp);
            } else {
                return handlePersonalInfoChange(request, user, clientIp);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour");
            response.put("logoutRequired", false);
            auditService.logProfileUpdateFailure(userEmail, "UNKNOWN", clientIp, e.getMessage());
        } finally {
            request.clearSensitiveData();
        }
        
        return response;
    }

    private Map<String, Object> handlePasswordChange(ProfileUpdateRequestDto request, User user, String clientIp, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        if (request.getNewPassword().length() < 12 || request.getNewPassword().length() > 128) {
            response.put("success", false);
            response.put("message", "Le nouveau mot de passe doit contenir entre 12 et 128 caractères");
            response.put("logoutRequired", false);
            return response;
        }
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            response.put("success", false);
            response.put("message", "Mot de passe actuel incorrect");
            response.put("logoutRequired", false);
            return response;
        }
        
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            response.put("success", false);
            response.put("message", "Les nouveaux mots de passe ne correspondent pas");
            response.put("logoutRequired", false);
            return response;
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        sessionService.invalidateSession(httpRequest);
        emailService.sendPasswordChangeNotification(user.getEmail(), user.getFirstName(), clientIp);
        auditService.logProfileUpdateSuccess(user.getEmail(), "PASSWORD_CHANGE", clientIp);
        
        response.put("success", true);
        response.put("message", "Mot de passe modifié avec succès. Vous allez être déconnecté.");
        response.put("logoutRequired", true);
        
        return response;
    }

    private Map<String, Object> handleEmailChange(ProfileUpdateRequestDto request, User user, String clientIp) {
        Map<String, Object> response = new HashMap<>();
        
        if (!request.getNewEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            response.put("success", false);
            response.put("message", "Format email invalide");
            response.put("logoutRequired", false);
            return response;
        }
        
        if (userRepository.findByEmail(request.getNewEmail()).isPresent()) {
            response.put("success", false);
            response.put("message", "Cet email est déjà utilisé par un autre compte");
            response.put("logoutRequired", false);
            return response;
        }
        
        String oldEmail = user.getEmail();
        user.setPendingEmailEncrypted(encryptionService.encryptSensitiveData(request.getNewEmail()));
        userRepository.save(user);
        
        // Generate OTP for new email confirmation
        String confirmationOtp = generateSecureOtp();
        String otpHash = passwordEncoder.encode(confirmationOtp);
        OtpToken confirmationToken = new OtpToken(request.getNewEmail(), otpHash, 5);
        otpTokenRepository.save(confirmationToken);
        
        emailService.sendEmailConfirmationOtp(request.getNewEmail(), user.getFirstName(), confirmationOtp);
        
        response.put("success", true);
        response.put("message", "Code de confirmation envoyé à " + request.getNewEmail() + ". Saisissez-le pour confirmer.");
        response.put("requiresConfirmation", true);
        response.put("logoutRequired", false);
        
        return response;
    }

    private Map<String, Object> handlePhoneChange(ProfileUpdateRequestDto request, User user, String clientIp) {
        Map<String, Object> response = new HashMap<>();
        
        if (!request.getNewPhone().matches("^\\+?[1-9]\\d{1,14}$")) {
            response.put("success", false);
            response.put("message", "Format téléphone invalide");
            response.put("logoutRequired", false);
            return response;
        }
        
        user.setPhoneEncrypted(encryptionService.encryptSensitiveData(request.getNewPhone()));
        userRepository.save(user);
        
        auditService.logProfileUpdateSuccess(user.getEmail(), "PHONE_CHANGE", clientIp);
        
        response.put("success", true);
        response.put("message", "Numéro de téléphone mis à jour avec succès");
        response.put("logoutRequired", false);
        
        return response;
    }

    private Map<String, Object> handlePersonalInfoChange(ProfileUpdateRequestDto request, User user, String clientIp) {
        Map<String, Object> response = new HashMap<>();
        
        if (request.getNewAddress() != null && !request.getNewAddress().trim().isEmpty()) {
            user.setAddressEncrypted(encryptionService.encryptSensitiveData(request.getNewAddress()));
        }
        if (request.getNewCountry() != null && !request.getNewCountry().trim().isEmpty()) {
            user.setCountryEncrypted(encryptionService.encryptSensitiveData(request.getNewCountry()));
        }
        
        userRepository.save(user);
        auditService.logProfileUpdateSuccess(user.getEmail(), "PERSONAL_INFO_CHANGE", clientIp);
        
        response.put("success", true);
        response.put("message", "Informations personnelles mises à jour avec succès");
        response.put("logoutRequired", false);
        
        return response;
    }

    private boolean isPasswordChange(ProfileUpdateRequestDto request) {
        return request.getCurrentPassword() != null && !request.getCurrentPassword().trim().isEmpty() &&
               request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty();
    }

    private boolean isEmailChange(ProfileUpdateRequestDto request) {
        return request.getNewEmail() != null && !request.getNewEmail().trim().isEmpty();
    }

    private boolean isPhoneChange(ProfileUpdateRequestDto request) {
        return request.getNewPhone() != null && !request.getNewPhone().trim().isEmpty();
    }

    public Map<String, Object> confirmEmailChange(String userEmail, String confirmationOtp, String clientIp) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            if (user.getPendingEmailEncrypted() == null) {
                response.put("success", false);
                response.put("message", "Aucun changement d'email en attente");
                response.put("logoutRequired", false);
                return response;
            }
            
            String pendingEmail = encryptionService.decryptSensitiveData(user.getPendingEmailEncrypted());
            
            // Validate OTP for the new email
            List<OtpToken> validTokens = otpTokenRepository.findValidOtpTokensByEmail(pendingEmail, LocalDateTime.now());
            boolean otpValid = false;
            
            for (OtpToken token : validTokens) {
                if (passwordEncoder.matches(confirmationOtp, token.getOtpCodeHash())) {
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                    otpValid = true;
                    break;
                }
            }
            
            if (!otpValid) {
                response.put("success", false);
                response.put("message", "Code de confirmation invalide ou expiré");
                response.put("logoutRequired", false);
                return response;
            }
            
            // Change email and clear pending
            String oldEmail = user.getEmail();
            user.setEmail(pendingEmail);
            user.setPendingEmailEncrypted(null);
            userRepository.save(user);
            
            // Update the session authentication with new email
            sessionService.updateSessionEmail(oldEmail, pendingEmail);
            
            emailService.sendEmailChangeNotification(oldEmail, user.getFirstName(), clientIp);
            auditService.logProfileUpdateSuccess(user.getEmail(), "EMAIL_CHANGE_CONFIRMED", clientIp);
            
            response.put("success", true);
            response.put("message", "Email modifié avec succès");
            response.put("logoutRequired", false);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la confirmation");
            response.put("logoutRequired", false);
        }
        
        return response;
    }

    private String generateSecureOtp() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
}