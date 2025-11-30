package com.securebank.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class SessionIpBindingFilter implements Filter {
    
    private static final String SESSION_IP_KEY = "SESSION_IP_ADDRESS";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        if (session != null) {
            String currentIp = getClientIpAddress(httpRequest);
            String sessionIp = (String) session.getAttribute(SESSION_IP_KEY);
            
            if (sessionIp == null) {
                // First time - bind IP to session
                session.setAttribute(SESSION_IP_KEY, currentIp);
            } else if (!sessionIp.equals(currentIp)) {
                // IP changed - invalidate session
                session.invalidate();
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("{\"error\":\"Session invalidated due to IP change\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}