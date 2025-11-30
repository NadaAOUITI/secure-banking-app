package com.securebank.config;

import com.securebank.interceptor.ValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import com.securebank.security.SessionIpBindingFilter;
import org.springframework.context.annotation.Bean;

@Configuration
public class ValidationConfig implements WebMvcConfigurer {
    
    private final ValidationInterceptor validationInterceptor;
    private final SessionIpBindingFilter sessionIpBindingFilter;
    
    @Autowired
    public ValidationConfig(ValidationInterceptor validationInterceptor, SessionIpBindingFilter sessionIpBindingFilter) {
        this.validationInterceptor = validationInterceptor;
        this.sessionIpBindingFilter = sessionIpBindingFilter;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(validationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/check-email");
    }
    
    @Bean
    public FilterRegistrationBean<SessionIpBindingFilter> ipBindingFilter() {
        FilterRegistrationBean<SessionIpBindingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(sessionIpBindingFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}