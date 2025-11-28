package com.securebank.controller;

import com.securebank.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/session")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SessionController {
    
    @Autowired
    private SessionService sessionService;
    
    /**
     * Vérifie le statut de la session
     */
    @GetMapping("/status")
    public ResponseEntity<?> getSessionStatus(HttpServletRequest request) {
        if (sessionService.isSessionValid(request)) {
            String userEmail = sessionService.getUserEmailFromSession(request);
            return ResponseEntity.ok().body(new SessionStatusResponse(true, userEmail, "Session active"));
        } else {
            return ResponseEntity.ok().body(new SessionStatusResponse(false, null, "Session expirée"));
        }
    }
    
    /**
     * Déconnexion manuelle
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        sessionService.invalidateSession(request);
        return ResponseEntity.ok().body(new SessionStatusResponse(false, null, "Déconnexion réussie"));
    }
    
    /**
     * Prolonger la session (heartbeat)
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(HttpServletRequest request) {
        if (sessionService.isSessionValid(request)) {
            sessionService.updateSessionActivity(request);
            return ResponseEntity.ok().body(new SessionStatusResponse(true, null, "Session prolongée"));
        } else {
            return ResponseEntity.ok().body(new SessionStatusResponse(false, null, "Session expirée"));
        }
    }
    
    /**
     * Informations de session (debug)
     */
    @GetMapping("/info")
    public ResponseEntity<String> getSessionInfo(HttpServletRequest request) {
        return ResponseEntity.ok(sessionService.getSessionInfo(request));
    }
    
    // Classe pour les réponses JSON
    public static class SessionStatusResponse {
        public boolean isValid;
        public String userEmail;
        public String message;
        
        public SessionStatusResponse(boolean isValid, String userEmail, String message) {
            this.isValid = isValid;
            this.userEmail = userEmail;
            this.message = message;
        }
    }
}