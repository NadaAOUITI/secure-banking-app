package com.securebank.service;

import com.securebank.model.BankAccount;
import com.securebank.model.User;
import com.securebank.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardSecurityTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private CardValidationService cardValidationService;

    @InjectMocks
    private RateLimitingService rateLimitingService;

    @InjectMocks
    private AntiReplayService antiReplayService;

    private BankAccount testAccount;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testAccount = new BankAccount();
        testAccount.setId(1L);
        testAccount.setUser(testUser);
        testAccount.setActive(true);
    }

    @Test
    void testValidCardAddition() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.countActiveCardsByAccountId(1L)).thenReturn(1L);

        CardValidationService.ValidationResult result = 
            cardValidationService.validateCardAddition(1L, "CLASSIC", "1357");

        assertTrue(result.isValid());
        assertEquals("Validation réussie", result.getMessage());
    }

    @Test
    void testWeakPinRejection() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.countActiveCardsByAccountId(1L)).thenReturn(1L);

        // Test repeated digits
        CardValidationService.ValidationResult result1 = 
            cardValidationService.validateCardAddition(1L, "CLASSIC", "1111");
        assertFalse(result1.isValid());
        assertTrue(result1.getMessage().contains("trop faible"));

        // Test sequential digits
        CardValidationService.ValidationResult result2 = 
            cardValidationService.validateCardAddition(1L, "CLASSIC", "1234");
        assertFalse(result2.isValid());
        assertTrue(result2.getMessage().contains("trop faible"));
    }

    @Test
    void testMaxCardsPerAccountLimit() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.countActiveCardsByAccountId(1L)).thenReturn(3L);

        CardValidationService.ValidationResult result = 
            cardValidationService.validateCardAddition(1L, "CLASSIC", "1357");

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Nombre maximum"));
    }

    @Test
    void testRateLimitingMechanism() {
        String identifier = "test@example.com:192.168.1.1";

        // Should allow initially
        assertTrue(rateLimitingService.isAllowed(identifier));

        // Record 3 failed attempts
        for (int i = 0; i < 3; i++) {
            rateLimitingService.recordAttempt(identifier, false);
        }

        // Should be blocked after 3 failures
        assertFalse(rateLimitingService.isAllowed(identifier));
        assertEquals(0, rateLimitingService.getRemainingAttempts(identifier));
    }

    @Test
    void testAntiReplayTokenGeneration() {
        String token = antiReplayService.generateToken("test@example.com", 1L);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Token should be valid once
        assertTrue(antiReplayService.validateAndConsumeToken(token, "test@example.com", 1L));
        
        // Same token should not be valid again (replay protection)
        assertFalse(antiReplayService.validateAndConsumeToken(token, "test@example.com", 1L));
    }

    @Test
    void testAntiReplayTokenValidation() {
        String token = antiReplayService.generateToken("test@example.com", 1L);
        
        // Valid token with correct parameters
        assertTrue(antiReplayService.validateAndConsumeToken(token, "test@example.com", 1L));
        
        // Invalid token with wrong email
        String token2 = antiReplayService.generateToken("test@example.com", 1L);
        assertFalse(antiReplayService.validateAndConsumeToken(token2, "wrong@example.com", 1L));
        
        // Invalid token with wrong account ID
        String token3 = antiReplayService.generateToken("test@example.com", 1L);
        assertFalse(antiReplayService.validateAndConsumeToken(token3, "test@example.com", 2L));
    }

    @Test
    void testInvalidCardType() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.countActiveCardsByAccountId(1L)).thenReturn(1L);

        CardValidationService.ValidationResult result = 
            cardValidationService.validateCardAddition(1L, "INVALID_TYPE", "1357");

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("invalide"));
    }

    @Test
    void testNonExistentAccount() {
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        CardValidationService.ValidationResult result = 
            cardValidationService.validateCardAddition(999L, "CLASSIC", "1357");

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("inexistant"));
    }
}