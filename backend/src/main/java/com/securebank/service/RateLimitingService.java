package com.securebank.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitingService {
    
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 15;
    
    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String identifier) {
        AttemptInfo info = attempts.get(identifier);
        if (info == null) return true;
        
        if (info.isLocked() && info.lockExpiry.isAfter(LocalDateTime.now())) {
            return false;
        }
        
        if (info.lockExpiry.isBefore(LocalDateTime.now())) {
            attempts.remove(identifier);
            return true;
        }
        
        return !info.isLocked();
    }
    
    public void recordAttempt(String identifier, boolean success) {
        if (success) {
            attempts.remove(identifier);
            return;
        }
        
        AttemptInfo info = attempts.computeIfAbsent(identifier, k -> new AttemptInfo());
        int currentAttempts = info.failedAttempts.incrementAndGet();
        
        if (currentAttempts >= MAX_ATTEMPTS) {
            info.locked = true;
            info.lockExpiry = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
        }
    }
    
    public int getRemainingAttempts(String identifier) {
        AttemptInfo info = attempts.get(identifier);
        if (info == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - info.failedAttempts.get());
    }
    
    private static class AttemptInfo {
        AtomicInteger failedAttempts = new AtomicInteger(0);
        boolean locked = false;
        LocalDateTime lockExpiry = LocalDateTime.now();
        
        boolean isLocked() {
            return locked && lockExpiry.isAfter(LocalDateTime.now());
        }
    }
}