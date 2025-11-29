package com.securebank.repository;

import com.securebank.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    /**
     * Trouve tous les tokens OTP valides par email (pour vérification avec hash)
     */
    @Query("SELECT o FROM OtpToken o WHERE o.email = ?1 AND o.used = false AND o.expiresAt > ?2")
    List<OtpToken> findValidOtpTokensByEmail(String email, LocalDateTime now);
    
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