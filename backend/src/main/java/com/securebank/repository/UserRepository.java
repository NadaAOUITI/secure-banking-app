package com.securebank.repository;

import com.securebank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Trouve un utilisateur par son email
     * @param email l'email de l'utilisateur
     * @return Optional contenant l'utilisateur s'il existe
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Vérifie si un email existe déjà
     * @param email l'email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);
}