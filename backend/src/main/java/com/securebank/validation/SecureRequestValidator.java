package com.securebank.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import com.securebank.dto.SecureRequestDto;

@Component
public class SecureRequestValidator implements Validator {
    
    private final ValidationService validationService;
    
    @Autowired
    public SecureRequestValidator(ValidationService validationService) {
        this.validationService = validationService;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return SecureRequestDto.class.isAssignableFrom(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof SecureRequestDto) {
            SecureRequestDto dto = (SecureRequestDto) target;
            validateSecureRequest(dto, errors);
        }
    }
    
    private void validateSecureRequest(SecureRequestDto dto, Errors errors) {
        // Validate anti-replay token
        if (dto.getAntiReplayToken() == null || dto.getAntiReplayToken().trim().isEmpty()) {
            errors.rejectValue("antiReplayToken", "required", "Security token required");
        }
        
        // Validate timestamp
        if (dto.getTimestamp() == null) {
            errors.rejectValue("timestamp", "required", "Timestamp required");
        } else {
            long currentTime = System.currentTimeMillis();
            long requestTime = dto.getTimestamp();
            long timeDiff = Math.abs(currentTime - requestTime);
            
            // Request must be within 5 minutes
            if (timeDiff > 300000) {
                errors.rejectValue("timestamp", "expired", "Request expired");
            }
        }
    }
}