package com.securebank.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SecurityAuditService {
    
    public void logCardAddition(String userEmail, Long accountId, String cardType, String ipAddress, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        String severity = success ? "INFO" : "WARNING";
        System.out.println(String.format(
            "[SECURITY AUDIT] [%s] Card Addition %s - User: %s, Account: %s, CardType: %s, IP: %s, Timestamp: %s",
            severity, status, maskEmail(userEmail), accountId, cardType, ipAddress, LocalDateTime.now()
        ));
        
        if (!success) {
            System.out.println(String.format(
                "[SECURITY ALERT] Failed card addition attempt detected - User: %s, IP: %s",
                maskEmail(userEmail), ipAddress
            ));
        }
    }
    
    public void logRateLimitExceeded(String userEmail, String ipAddress, String operation) {
        System.out.println(String.format(
            "[SECURITY ALERT] [HIGH] Rate limit exceeded - User: %s, Operation: %s, IP: %s, Timestamp: %s",
            maskEmail(userEmail), operation, ipAddress, LocalDateTime.now()
        ));
    }
    
    public void logReplayTokenViolation(String userEmail, String ipAddress) {
        System.out.println(String.format(
            "[SECURITY ALERT] [CRITICAL] Replay token violation - User: %s, IP: %s, Timestamp: %s",
            maskEmail(userEmail), ipAddress, LocalDateTime.now()
        ));
    }
    
    public void logSuspiciousActivity(String userEmail, String activity, String ipAddress) {
        System.out.println(String.format(
            "[SECURITY ALERT] Suspicious Activity - User: %s, Activity: %s, IP: %s, Timestamp: %s",
            userEmail, activity, ipAddress, LocalDateTime.now()
        ));
    }
    
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 2) return email;
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + parts[1];
    }
}