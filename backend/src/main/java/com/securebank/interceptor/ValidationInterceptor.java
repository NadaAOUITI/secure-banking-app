package com.securebank.interceptor;

import com.securebank.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@Component
public class ValidationInterceptor implements HandlerInterceptor {
    
    private final ValidationService validationService;
    
    @Autowired
    public ValidationInterceptor(ValidationService validationService) {
        this.validationService = validationService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Validate all request parameters
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            
            if (paramValue != null) {
                ValidationService.ValidationResult result = validationService.validateAndSanitize(paramValue, ValidationService.ValidationType.TEXT);
                if (!result.isValid()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"success\":false,\"message\":\"Invalid input detected\"}");
                    return false;
                }
            }
        }
        
        return true;
    }
}