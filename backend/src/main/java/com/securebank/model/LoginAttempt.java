package com.securebank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
public class LoginAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private boolean successful;
    
    @Column(nullable = false)
    private LocalDateTime attemptTime;
    
    @Column(length = 500)
    private String failureReason;
    
    // Constructeurs
    public LoginAttempt() {}
    
    public LoginAttempt(String email, String ipAddress, boolean successful, String failureReason) {
        this.email = email;
        this.ipAddress = ipAddress;
        this.successful = successful;
        this.failureReason = failureReason;
        this.attemptTime = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
    
    public LocalDateTime getAttemptTime() { return attemptTime; }
    public void setAttemptTime(LocalDateTime attemptTime) { this.attemptTime = attemptTime; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}