package com.securebank.config;

import org.springframework. context.annotation.Bean;
import org. springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation. web.builders.HttpSecurity;
import org.springframework.security.config.annotation. web.configuration.EnableWebSecurity;
import org.springframework. security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security. crypto.password. PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context. HttpSessionSecurityContextRepository;
import org.springframework.security. web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web. cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuration de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                . cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        . sessionCreationPolicy(org.springframework.security.config.http. SessionCreationPolicy. ALWAYS)
                        .sessionFixation(). none()
                        . maximumSessions(1)
                        . maxSessionsPreventsLogin(false)
                )
                .authorizeHttpRequests(authz -> authz
                        // Permettre les requêtes OPTIONS (CORS preflight)
                        . requestMatchers(org.springframework.http. HttpMethod.OPTIONS, "/**").permitAll()

                        // Routes publiques (pas d'authentification requise)
                        . requestMatchers("/api/auth/**").permitAll()
                        . requestMatchers("/api/onboarding/**").permitAll()

                        // Routes protégées (authentification requise)
                        .requestMatchers("/api/account/**").authenticated()
                        .requestMatchers("/api/accounts/**").authenticated()
                        .requestMatchers("/api/beneficiaries/**").authenticated()
                        .requestMatchers("/api/transfers/**").authenticated()

                        // Toutes les autres requêtes nécessitent une authentification
                        . anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Force de 12 pour plus de sécurité
    }

    /**
     * Repository pour le contexte de sécurité (sessions)
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Configuration CORS pour permettre les requêtes du frontend React
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays. asList("http://localhost:3000")); // Frontend React
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration. setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}