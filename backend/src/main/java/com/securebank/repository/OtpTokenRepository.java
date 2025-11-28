package com.securebank.repository;

import com.securebank.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    /**
     * Trouve un token OTP valide par email et code
     */
    @Query("SELECT o FROM OtpToken o WHERE o.email = ?1 AND o.otpCode = ?2 AND o.used = false AND o.expiresAt > ?3")
    Optional<OtpToken> findValidOtpToken(String email, String otpCode, LocalDateTime now);
    
    /**
     * Supprime tous les tokens expirés
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < ?1")
    void deleteExpiredTokens(LocalDateTime now);
    
    /**
     * Marque tous les tokens d'un email comme utilisés
     */
    @Modifying
    @Transactional
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.email = ?1")
    void markAllTokensAsUsed(String email);
}