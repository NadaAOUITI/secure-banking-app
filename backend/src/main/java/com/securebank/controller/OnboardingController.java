package com.securebank.controller;

import com.securebank.dto.UserRegistrationDto;
import com.securebank.service.UserService;
import com.securebank.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "http://localhost:3000")
public class OnboardingController {
    
    private final UserService userService;
    private final OtpService otpService;
    
    @Autowired
    public OnboardingController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }
    
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeOnboarding(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", "Données invalides");
                return ResponseEntity.badRequest().body(response);
            }
            
            var user = userService.registerUser(registrationDto);
            
            response.put("success", true);
            response.put("message", "Onboarding terminé avec succès");
            response.put("userId", user.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (userService.emailExists(email)) {
                response.put("success", false);
                response.put("message", "Un compte avec cet email existe déjà");
                return ResponseEntity.badRequest().body(response);
            }
            
            otpService.generateAndSendOnboardingOtp(email);
            
            response.put("success", true);
            response.put("message", "Code de vérification envoyé");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String otpCode = request.get("otpCode");
            
            if (email == null || otpCode == null) {
                response.put("success", false);
                response.put("message", "Email et code requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean isValid = otpService.verifyOtp(email, otpCode);
            
            if (isValid) {
                response.put("success", true);
                response.put("message", "Email vérifié avec succès");
            } else {
                response.put("success", false);
                response.put("message", "Code invalide ou expiré");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}