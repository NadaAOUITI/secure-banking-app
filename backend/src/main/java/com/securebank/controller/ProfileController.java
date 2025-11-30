package com.securebank.controller;

import com.securebank.dto.ProfileUpdateRequestDto;
import com.securebank.service.ProfileUpdateService;
import com.securebank.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProfileController {

    private final ProfileUpdateService profileUpdateService;
    private final SecurityAuditService auditService;

    @Autowired
    public ProfileController(ProfileUpdateService profileUpdateService, SecurityAuditService auditService) {
        this.profileUpdateService = profileUpdateService;
        this.auditService = auditService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("✅ Profile controller test endpoint hit!");
        return ResponseEntity.ok("Profile controller is working!");
    }

    @PostMapping("/initiate-update")
    public ResponseEntity<Map<String, Object>> initiateUpdate(HttpServletRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                System.out.println("❌ Auth failed: " + (auth == null ? "null" : "not authenticated"));
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Session expirée"));
            }

            System.out.println("✅ Initiate profile update for: " + auth.getName());
            String clientIp = auditService.getClientIpAddress(request);
            Map<String, Object> response = profileUpdateService.initiateProfileUpdate(auth.getName(), clientIp);
            
            System.out.println("📝 Response: " + response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error in initiate-update: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Erreur serveur: " + e.getMessage()));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequestDto updateRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Session expirée"));
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Données invalides: " + bindingResult.getFieldError().getDefaultMessage()
            ));
        }

        String clientIp = auditService.getClientIpAddress(request);
        Map<String, Object> response = profileUpdateService.updateProfile(updateRequest, auth.getName(), clientIp, request);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<Map<String, Object>> confirmEmailChange(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Session expirée"));
        }

        String confirmationOtp = requestBody.get("confirmationOtp");
        String clientIp = auditService.getClientIpAddress(request);
        
        Map<String, Object> response = profileUpdateService.confirmEmailChange(auth.getName(), confirmationOtp, clientIp);
        
        return ResponseEntity.ok(response);
    }
}