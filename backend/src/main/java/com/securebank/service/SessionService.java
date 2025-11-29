package com.securebank.service;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service
public class SessionService {
    
    private static final int SESSION_TIMEOUT = 900; // 15 minutes en secondes
    
    /**
     * Crée une nouvelle session sécurisée pour l'utilisateur
     */
    public void createUserSession(HttpServletRequest request, String userEmail) {
        HttpSession session = request.getSession(true);
        
        // Configuration sécurisée de la session
        session.setMaxInactiveInterval(SESSION_TIMEOUT);
        session.setAttribute("userEmail", userEmail);
        session.setAttribute("loginTime", LocalDateTime.now());
        session.setAttribute("lastActivity", LocalDateTime.now());
        
        System.out.println("✅ Session créée pour: " + userEmail + " (ID: " + session.getId() + ")");
    }
    
    /**
     * Met à jour l'activité de la session
     */
    public void updateSessionActivity(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("lastActivity", LocalDateTime.now());
        }
    }
    
    /**
     * Vérifie si la session est valide et active
     */
    public boolean isSessionValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return false;
        }
        
        String userEmail = (String) session.getAttribute("userEmail");
        LocalDateTime lastActivity = (LocalDateTime) session.getAttribute("lastActivity");
        
        if (userEmail == null || lastActivity == null) {
            return false;
        }
        
        // Vérifier si la session a expiré (15 minutes d'inactivité)
        LocalDateTime now = LocalDateTime.now();
        if (lastActivity.plusMinutes(15).isBefore(now)) {
            invalidateSession(request);
            return false;
        }
        
        return true;
    }
    
    /**
     * Invalide la session utilisateur
     */
    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userEmail = (String) session.getAttribute("userEmail");
            session.invalidate();
            System.out.println("🔒 Session invalidée pour: " + userEmail);
        }
    }
    
    /**
     * Récupère l'email de l'utilisateur depuis la session
     */
    public String getUserEmailFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && isSessionValid(request)) {
            return (String) session.getAttribute("userEmail");
        }
        return null;
    }
    
    /**
     * Récupère les informations de session
     */
    public String getSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "Aucune session active";
        }
        
        String userEmail = (String) session.getAttribute("userEmail");
        LocalDateTime loginTime = (LocalDateTime) session.getAttribute("loginTime");
        LocalDateTime lastActivity = (LocalDateTime) session.getAttribute("lastActivity");
        
        return String.format("Session: %s | Utilisateur: %s | Connexion: %s | Dernière activité: %s", 
                           session.getId(), userEmail, loginTime, lastActivity);
    }
}