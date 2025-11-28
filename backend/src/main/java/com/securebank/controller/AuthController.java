package com.securebank.controller;

import com.securebank.dto.LoginRequestDto;
import com.securebank.dto.OtpVerificationDto;
import com.securebank.dto.UserRegistrationDto;
import com.securebank.exception.PasswordMismatchException;
import com.securebank.exception.UserAlreadyExistsException;
import com.securebank.model.User;
import com.securebank.service.AccountLockService;
import com.securebank.service.OtpService;
import com.securebank.service.SessionService;
import com.securebank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final UserService userService;
    private final OtpService otpService;
    private final SessionService sessionService;
    private final AccountLockService accountLockService;
    
    @Autowired
    public AuthController(UserService userService, OtpService otpService, SessionService sessionService, AccountLockService accountLockService) {
        this.userService = userService;
        this.otpService = otpService;
        this.sessionService = sessionService;
        this.accountLockService = accountLockService;
    }
    
    /**
     * Endpoint d'inscription d'un nouvel utilisateur
     * @param registrationDto les données d'inscription
     * @param bindingResult résultat de la validation
     * @return ResponseEntity avec le résultat de l'inscription
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier les erreurs de validation
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage(),
                                (existing, replacement) -> existing + "; " + replacement
                        ));
                
                response.put("success", false);
                response.put("message", "Erreurs de validation");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Enregistrer l'utilisateur
            User user = userService.registerUser(registrationDto);
            
            // Réponse de succès (sans exposer le mot de passe)
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("createdAt", user.getCreatedAt());
            
            response.put("success", true);
            response.put("message", "Inscription réussie");
            response.put("user", userData);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (UserAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            
        } catch (PasswordMismatchException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint pour vérifier si un email existe
     * @param email l'email à vérifier
     * @return ResponseEntity avec le résultat de la vérification
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        boolean exists = userService.emailExists(email);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de connexion - Étape 1: Vérification email/mot de passe
     * @param loginRequest les données de connexion
     * @param bindingResult résultat de la validation
     * @return ResponseEntity avec le résultat
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier les erreurs de validation
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage(),
                                (existing, replacement) -> existing + "; " + replacement
                        ));
                
                response.put("success", false);
                response.put("message", "Erreurs de validation");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            String email = loginRequest.getEmail();
            
            // Vérifier si le compte est verrouillé
            if (userService.isAccountLocked(email)) {
                long failedAttempts = userService.getFailedAttemptsCount(email);
                accountLockService.recordLoginAttempt(email, false, "Compte verrouillé", request);
                
                response.put("success", false);
                response.put("message", "Compte temporairement verrouillé après " + failedAttempts + " tentatives échouées. Réessayez dans 30 minutes.");
                response.put("accountLocked", true);
                return ResponseEntity.status(HttpStatus.LOCKED).body(response);
            }
            
            // Authentifier l'utilisateur
            User user = userService.authenticateUser(email, loginRequest.getPassword());
            
            if (user == null) {
                // Enregistrer la tentative échouée
                accountLockService.recordLoginAttempt(email, false, "Mot de passe incorrect", request);
                
                long failedAttempts = userService.getFailedAttemptsCount(email);
                String message = "Email ou mot de passe incorrect";
                
                if (failedAttempts >= 3) {
                    message += ". Attention: " + failedAttempts + "/5 tentatives échouées.";
                }
                
                response.put("success", false);
                response.put("message", message);
                response.put("failedAttempts", failedAttempts);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Enregistrer la tentative réussie
            accountLockService.recordLoginAttempt(email, true, null, request);
            
            // Générer et envoyer l'OTP
            otpService.generateAndSendOtp(user.getEmail());
            
            response.put("success", true);
            response.put("message", "Code de vérification envoyé par email");
            response.put("requiresOtp", true);
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint de vérification OTP - Étape 2: Vérification du code
     * @param otpRequest les données de vérification OTP
     * @param bindingResult résultat de la validation
     * @return ResponseEntity avec le résultat
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @Valid @RequestBody OtpVerificationDto otpRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier les erreurs de validation
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage(),
                                (existing, replacement) -> existing + "; " + replacement
                        ));
                
                response.put("success", false);
                response.put("message", "Erreurs de validation");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Vérifier l'OTP
            boolean isValidOtp = otpService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtpCode());
            
            if (!isValidOtp) {
                // Enregistrer la tentative OTP échouée
                accountLockService.recordLoginAttempt(otpRequest.getEmail(), false, "Code OTP invalide", request);
                
                response.put("success", false);
                response.put("message", "Code de vérification invalide ou expiré");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Récupérer l'utilisateur
            User user = userService.findByEmail(otpRequest.getEmail());
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "Utilisateur introuvable");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Créer une session sécurisée
            sessionService.createUserSession(request, user.getEmail());
            
            // Connexion réussie
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("lastLoginAt", user.getLastLoginAt());
            
            response.put("success", true);
            response.put("message", "Connexion réussie - Session créée");
            response.put("user", userData);
            response.put("sessionTimeout", 900); // 15 minutes
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}