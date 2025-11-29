package com.securebank.controller;

import com.securebank.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "http://localhost:3000")
public class OtpController {
    
    private final OtpService otpService;
    
    @Autowired
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }
    
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            otpService.generateAndSendOtp(email);
            
            response.put("success", true);
            response.put("message", "Code envoyé avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
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
                response.put("message", "Code vérifié avec succès");
            } else {
                response.put("success", false);
                response.put("message", "Code invalide ou expiré");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la vérification: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}