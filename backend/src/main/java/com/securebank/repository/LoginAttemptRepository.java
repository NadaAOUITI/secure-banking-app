package com.securebank.repository;

import com.securebank.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    
    /**
     * Compte les tentatives échouées pour un email dans une période donnée
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email " +
           "AND la.successful = false AND la.attemptTime >= :since")
    long countFailedAttemptsSince(@Param("email") String email, @Param("since") LocalDateTime since);
    
    /**
     * Trouve toutes les tentatives pour un email dans une période donnée
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email " +
           "AND la.attemptTime >= :since ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findAttemptsSince(@Param("email") String email, @Param("since") LocalDateTime since);
    
    /**
     * Trouve la dernière tentative réussie pour un email
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email " +
           "AND la.successful = true ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findLastSuccessfulAttempt(@Param("email") String email);
}