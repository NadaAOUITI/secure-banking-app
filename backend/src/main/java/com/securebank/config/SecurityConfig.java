package com.securebank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation. Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config. annotation.web.builders.HttpSecurity;
import org.springframework.security.config. annotation.web.configuration.EnableWebSecurity;
import org. springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework. security.crypto.password.PasswordEncoder;
import org.springframework.security.web. SecurityFilterChain;
import org.springframework.security.web. header.writers.ReferrerPolicyHeaderWriter;
import org. springframework.web.cors.CorsConfiguration;
import org.springframework.web. cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuration de la chaîne de filtres de sécurité
     * Inclut: HTTPS redirection, Security Headers, CORS, Authorization
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ==================== 1. HTTPS REDIRECTION ====================
                // Redirige toutes les requêtes HTTP vers HTTPS (en production)
                .requiresChannel(channel -> channel
                        . anyRequest().requiresSecure()
                )

                // ==================== 2. SECURITY HEADERS ====================
                .headers(headers -> headers
                        // HSTS - Force le navigateur à utiliser HTTPS pendant 1 an
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000) // 1 an
                                .preload(true)
                        )
                        // Empêche le clickjacking - ne permet pas l'affichage dans un iframe
                        .frameOptions(frame -> frame. deny())
                        // Empêche le MIME sniffing
                        .contentTypeOptions(contentType -> {})
                        // Politique de référent stricte
                        .referrerPolicy(referrer -> referrer
                                . policy(ReferrerPolicyHeaderWriter. ReferrerPolicy. STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                        // Content Security Policy - Protection XSS
                        . contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data:; " +
                                        "font-src 'self'; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'none'; " +
                                        "form-action 'self';")
                        )
                        // Permissions Policy - Désactive les fonctionnalités sensibles
                        .permissionsPolicy(permissions -> permissions
                                .policy("geolocation=(), microphone=(), camera=(), payment=()")
                        )
                )

                // ==================== 3. CORS CONFIGURATION ====================
                .cors(cors -> cors. configurationSource(corsConfigurationSource()))

                // ==================== 4.  CSRF CONFIGURATION ====================
                // Désactivé pour l'API REST stateless (utilise tokens à la place)
                .csrf(csrf -> csrf.disable())

                // ==================== 5.  AUTHORIZATION RULES ====================
                . authorizeHttpRequests(authz -> authz
                        . requestMatchers("/api/auth/**").permitAll()
                        . requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Configuration alternative pour le développement local (sans HTTPS)
     * Activée avec le profil "dev"
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
                // Pas de redirection HTTPS en dev
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> {})
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy. STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                )
                .cors(cors -> cors. configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt avec coût 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuration CORS sécurisée
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // En production, remplacer par le domaine réel
        configuration. setAllowedOriginPatterns(Arrays. asList("https://localhost:3000", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-XSRF-TOKEN"
        ));
        configuration.setExposedHeaders(Arrays.asList("X-XSRF-TOKEN"));
        configuration. setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight pendant 1 heure

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}