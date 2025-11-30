package com.securebank.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AntiReplayService {
    
    private static final int TOKEN_VALIDITY_MINUTES = 5;
    private final ConcurrentHashMap<String, LocalDateTime> usedTokens = new ConcurrentHashMap<>();
    
    public String generateToken(String userEmail, Long accountId) {
        String tokenData = userEmail + ":" + accountId + ":" + System.currentTimeMillis();
        return java.util.Base64.getEncoder().encodeToString(tokenData.getBytes());
    }
    
    public boolean validateAndConsumeToken(String token, String userEmail, Long accountId) {
        if (token == null || token.isEmpty()) return false;
        
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            
            if (parts.length != 3) return false;
            
            String tokenEmail = parts[0];
            Long tokenAccountId = Long.parseLong(parts[1]);
            long timestamp = Long.parseLong(parts[2]);
            
            // Validate token content
            if (!tokenEmail.equals(userEmail) || !tokenAccountId.equals(accountId)) {
                return false;
            }
            
            // Check if token is expired
            LocalDateTime tokenTime = LocalDateTime.now().minusNanos((System.currentTimeMillis() - timestamp) * 1_000_000);
            if (tokenTime.isBefore(LocalDateTime.now().minusMinutes(TOKEN_VALIDITY_MINUTES))) {
                return false;
            }
            
            // Check if token was already used
            if (usedTokens.containsKey(token)) {
                return false;
            }
            
            // Mark token as used
            usedTokens.put(token, LocalDateTime.now());
            
            // Cleanup old tokens
            cleanupExpiredTokens();
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(TOKEN_VALIDITY_MINUTES);
        usedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }
}