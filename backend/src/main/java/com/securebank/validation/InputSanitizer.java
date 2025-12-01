package com.securebank.validation;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class InputSanitizer {
    
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror|onclick)");
    
    public String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String sanitized = input.trim();
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = sanitized.replace("'", "&#39;")
                            .replace("\"", "&quot;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("&", "&amp;");
        
        return sanitized;
    }
    
    public String sanitizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase().replaceAll("[^a-zA-Z0-9@._-]", "");
    }
    
    public boolean containsSqlInjection(String input) {
        if (input == null) return false;
        return SQL_INJECTION_PATTERN.matcher(input.toLowerCase()).find();
    }
    
    public boolean containsXssAttempt(String input) {
        if (input == null) return false;
        return SCRIPT_PATTERN.matcher(input.toLowerCase()).find() || 
               input.toLowerCase().contains("javascript:") ||
               input.toLowerCase().contains("vbscript:");
    }
}