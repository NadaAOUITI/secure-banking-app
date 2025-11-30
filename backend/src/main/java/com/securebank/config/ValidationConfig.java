package com.securebank.config;

import com.securebank.interceptor.ValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ValidationConfig implements WebMvcConfigurer {
    
    private final ValidationInterceptor validationInterceptor;
    
    @Autowired
    public ValidationConfig(ValidationInterceptor validationInterceptor) {
        this.validationInterceptor = validationInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(validationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/check-email"); // Already has custom validation
    }
}