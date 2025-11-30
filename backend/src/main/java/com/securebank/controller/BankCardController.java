package com.securebank.controller;

import com.securebank.dto.AddCardRequest;
import com.securebank.model.BankAccount;
import com.securebank.model.BankCard;
import com.securebank.model.User;
import com.securebank.service.BankAccountService;
import com.securebank.repository.BankCardRepository;
import com.securebank.service.UserService;
import com.securebank.service.SecurityAuditService;
import com.securebank.service.RateLimitingService;
import com.securebank.service.CardValidationService;
import com.securebank.repository.BankAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "http://localhost:3000")
public class BankCardController {
    
    private final BankAccountService bankAccountService;
    private final UserService userService;
    private final SecurityAuditService securityAuditService;
    private final RateLimitingService rateLimitingService;
    private final CardValidationService cardValidationService;
    private final BankAccountRepository bankAccountRepository;
    private final BankCardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public BankCardController(BankAccountService bankAccountService, UserService userService, 
                             SecurityAuditService securityAuditService, RateLimitingService rateLimitingService,
                             CardValidationService cardValidationService, BankAccountRepository bankAccountRepository,
                             BankCardRepository cardRepository, PasswordEncoder passwordEncoder) {
        this.bankAccountService = bankAccountService;
        this.userService = userService;
        this.securityAuditService = securityAuditService;
        this.rateLimitingService = rateLimitingService;
        this.cardValidationService = cardValidationService;
        this.bankAccountRepository = bankAccountRepository;
        this.cardRepository = cardRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @GetMapping("/test-cards")
    public ResponseEntity<Map<String, Object>> testCards(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Session expirée");
                return ResponseEntity.status(401).body(response);
            }
            
            User user = userService.findByEmail(auth.getName());
            if (user == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.status(401).body(response);
            }
            
            // Test direct avec le repository
            List<BankCard> allUserCards = new ArrayList<>();
            List<BankAccount> accounts = bankAccountRepository.findByUserAndIsActiveTrue(user);
            
            for (BankAccount account : accounts) {
                List<BankCard> accountCards = cardRepository.findByAccountAndIsActiveTrue(account);
                allUserCards.addAll(accountCards);
            }
            
            List<Map<String, Object>> cardDetails = new ArrayList<>();
            for (BankCard card : allUserCards) {
                Map<String, Object> cardInfo = new HashMap<>();
                cardInfo.put("id", card.getId());
                cardInfo.put("accountId", card.getAccount().getId());
                cardInfo.put("cardType", card.getCardType().name());
                cardInfo.put("isActive", card.isActive());
                cardInfo.put("createdAt", card.getCreatedAt().toString());
                cardDetails.add(cardInfo);
            }
            
            response.put("success", true);
            response.put("totalCards", allUserCards.size());
            response.put("cards", cardDetails);
            response.put("totalAccounts", accounts.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/user-cards")
    public ResponseEntity<Map<String, Object>> getUserCards(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        String clientIp = securityAuditService.getClientIpAddress(httpRequest);
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Session expirée");
                return ResponseEntity.status(401).body(response);
            }
            
            User user = userService.findByEmail(auth.getName());
            if (user == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.status(401).body(response);
            }
            
            // Récupérer toutes les cartes de l'utilisateur via le repository
            List<BankAccount> accounts = bankAccountRepository.findByUserAndIsActiveTrue(user);
            List<Map<String, Object>> allCards = new ArrayList<>();
            
            for (BankAccount account : accounts) {
                List<BankCard> cards = bankAccountService.getCardsByAccount(account);
                
                for (BankCard card : cards) {
                    Map<String, Object> cardInfo = new HashMap<>();
                    cardInfo.put("id", card.getId());
                    cardInfo.put("accountId", account.getId());
                    cardInfo.put("accountNumber", account.getAccountNumber());
                    cardInfo.put("cardType", card.getCardType().name());
                    cardInfo.put("createdAt", card.getCreatedAt());
                    cardInfo.put("isActive", card.isActive());
                    allCards.add(cardInfo);
                }
            }
            
            response.put("success", true);
            response.put("cards", allCards);
            response.put("totalCards", allCards.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des cartes");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCard(
            @Valid @RequestBody AddCardRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        Map<String, Object> response = new HashMap<>();
        String clientIp = securityAuditService.getClientIpAddress(httpRequest);
        
        try {
            // 1. Authentication verification
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                securityAuditService.logSuspiciousActivity("UNKNOWN", "Unauthenticated card addition attempt", clientIp);
                response.put("success", false);
                response.put("message", "Session expirée. Veuillez vous reconnecter.");
                return ResponseEntity.status(401).body(response);
            }
            
            // 2. Rate limiting check
            String rateLimitKey = auth.getName() + ":" + clientIp;
            if (!rateLimitingService.isAllowed(rateLimitKey)) {
                securityAuditService.logSuspiciousActivity(auth.getName(), 
                    "Rate limit exceeded for card addition", clientIp);
                response.put("success", false);
                response.put("message", "Trop de tentatives. Veuillez réessayer dans 15 minutes.");
                return ResponseEntity.status(429).body(response);
            }
            
            User user = userService.findByEmail(auth.getName());
            if (user == null) {
                rateLimitingService.recordAttempt(rateLimitKey, false);
                securityAuditService.logSuspiciousActivity(auth.getName(), "User not found during card addition", clientIp);
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé.");
                return ResponseEntity.status(401).body(response);
            }
            
            // 3. Input validation
            if (bindingResult.hasErrors()) {
                rateLimitingService.recordAttempt(rateLimitKey, false);
                securityAuditService.logCardAddition(user.getEmail(), request.getAccountId(), 
                    request.getCardType(), clientIp, false);
                response.put("success", false);
                response.put("message", "Données invalides: " + bindingResult.getFieldError().getDefaultMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // 4. Password verification (step-up authentication)
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                rateLimitingService.recordAttempt(rateLimitKey, false);
                securityAuditService.logSuspiciousActivity(user.getEmail(), 
                    "Invalid password for card addition", clientIp);
                response.put("success", false);
                response.put("message", "Mot de passe incorrect.");
                response.put("remainingAttempts", rateLimitingService.getRemainingAttempts(rateLimitKey));
                return ResponseEntity.status(403).body(response);
            }
            
            // 5. Account ownership verification
            BankAccount account = bankAccountRepository.findById(request.getAccountId())
                .orElse(null);
            
            if (account == null) {
                rateLimitingService.recordAttempt(rateLimitKey, false);
                response.put("success", false);
                response.put("message", "Compte inexistant.");
                return ResponseEntity.status(404).body(response);
            }
            
            if (!account.getUser().getId().equals(user.getId())) {
                rateLimitingService.recordAttempt(rateLimitKey, false);
                securityAuditService.logSuspiciousActivity(user.getEmail(), 
                    "Attempted to add card to unauthorized account: " + request.getAccountId(), clientIp);
                response.put("success", false);
                response.put("message", "Compte non autorisé.");
                return ResponseEntity.status(403).body(response);
            }
            
            if (!account.isActive()) {
                rateLimitingService.recordAttempt(rateLimitKey, false);
                response.put("success", false);
                response.put("message", "Compte inactif.");
                return ResponseEntity.status(403).body(response);
            }
            
            // 6. Advanced card validation
            CardValidationService.ValidationResult validation = cardValidationService.validateCardAddition(
                request.getAccountId(), request.getCardType(), request.getPin());
            
            if (!validation.isValid()) {
                rateLimitingService.recordAttempt(rateLimitKey, false);
                securityAuditService.logCardAddition(user.getEmail(), account.getId(), 
                    request.getCardType(), clientIp, false);
                response.put("success", false);
                response.put("message", validation.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // 7. Create card securely
            BankCard newCard = bankAccountService.createCards(account, 
                new String[]{request.getCardType()}, 
                new String[]{request.getPin()}).get(0);
            
            // 8. Success logging and rate limit reset
            rateLimitingService.recordAttempt(rateLimitKey, true);
            securityAuditService.logCardAddition(user.getEmail(), account.getId(), 
                request.getCardType(), clientIp, true);
            
            response.put("success", true);
            response.put("message", "Carte ajoutée avec succès");
            response.put("cardType", newCard.getCardType().getDisplayName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String rateLimitKey = (auth != null ? auth.getName() : "UNKNOWN") + ":" + clientIp;
            rateLimitingService.recordAttempt(rateLimitKey, false);
            securityAuditService.logCardAddition(auth != null ? auth.getName() : "UNKNOWN", 
                request.getAccountId(), request.getCardType(), clientIp, false);
            response.put("success", false);
            response.put("message", "Erreur lors de l'ajout de la carte");
            return ResponseEntity.status(500).body(response);
        } finally {
            // Nettoyage des données sensibles
            request.clearSensitiveData();
        }
    }
}