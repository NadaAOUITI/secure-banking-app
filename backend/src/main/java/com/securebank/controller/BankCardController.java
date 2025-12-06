package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.exception.*;
import com.securebank.model. BankAccount;
import com.securebank.model.BankCard;
import com.securebank.model.Transaction;
import com. securebank.model.User;
import com.securebank.service.*;
import com.securebank.repository. BankCardRepository;
import com.securebank. repository.BankAccountRepository;
import com.securebank. repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org. springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context. SecurityContextHolder;
import org.springframework. security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org. springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util. HashMap;
import java.util.Map;
import java. util.List;
import java.util. ArrayList;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"})
public class BankCardController {

    private final BankAccountService bankAccountService;
    private final UserService userService;
    private final SecurityAuditService securityAuditService;
    private final RateLimitingService rateLimitingService;
    private final CardValidationService cardValidationService;
    private final BankAccountRepository bankAccountRepository;
    private final BankCardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public BankCardController(BankAccountService bankAccountService,
                              UserService userService,
                              SecurityAuditService securityAuditService,
                              RateLimitingService rateLimitingService,
                              CardValidationService cardValidationService,
                              BankAccountRepository bankAccountRepository,
                              BankCardRepository cardRepository,
                              TransactionRepository transactionRepository,
                              PasswordEncoder passwordEncoder,
                              EmailService emailService) {
        this.bankAccountService = bankAccountService;
        this. userService = userService;
        this. securityAuditService = securityAuditService;
        this.rateLimitingService = rateLimitingService;
        this.cardValidationService = cardValidationService;
        this.bankAccountRepository = bankAccountRepository;
        this. cardRepository = cardRepository;
        this. transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ==================== ENDPOINTS DE RÉCUPÉRATION ====================

    @GetMapping("/test-cards")
    public ResponseEntity<Map<String, Object>> testCards(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Session expirée");
                return ResponseEntity.status(401). body(response);
            }

            List<BankCard> allUserCards = new ArrayList<>();
            List<BankAccount> accounts = bankAccountRepository. findByUserAndIsActiveTrue(user);

            for (BankAccount account : accounts) {
                List<BankCard> accountCards = cardRepository. findByAccountAndIsActiveTrue(account);
                allUserCards.addAll(accountCards);
            }

            List<Map<String, Object>> cardDetails = new ArrayList<>();
            for (BankCard card : allUserCards) {
                Map<String, Object> cardInfo = new HashMap<>();
                cardInfo.put("id", card.getId());
                cardInfo. put("accountId", card.getAccount().getId());
                cardInfo.put("cardType", card. getCardType(). name());
                cardInfo.put("isActive", card.isActive());
                cardInfo.put("createdAt", card. getCreatedAt(). toString());
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
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user-cards")
    public ResponseEntity<Map<String, Object>> getUserCards(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Session expirée");
                return ResponseEntity.status(401).body(response);
            }

            List<BankAccount> accounts = bankAccountRepository. findByUserAndIsActiveTrue(user);
            List<Map<String, Object>> allCards = new ArrayList<>();

            for (BankAccount account : accounts) {
                List<BankCard> cards = bankAccountService.getCardsByAccount(account);

                for (BankCard card : cards) {
                    Map<String, Object> cardInfo = new HashMap<>();
                    cardInfo.put("id", card.getId());
                    cardInfo.put("accountId", account.getId());
                    cardInfo.put("accountNumber", account.getAccountNumber());
                    cardInfo.put("cardType", card.getCardType().name());
                    cardInfo. put("createdAt", card. getCreatedAt());
                    cardInfo.put("isActive", card.isActive());
                    cardInfo.put("isBlocked", card.isBlocked());
                    allCards.add(cardInfo);
                }
            }

            response.put("success", true);
            response.put("cards", allCards);
            response.put("totalCards", allCards.size());

            return ResponseEntity. ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des cartes");
            return ResponseEntity.status(500). body(response);
        }
    }

    // ==================== ENDPOINTS DE SÉCURITÉ ====================

    @PostMapping("/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyPin(
            @Valid @RequestBody PinVerificationRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED). body(response);
            }

            BankCard card = cardRepository. findById(request. getCardId()). orElse(null);

            if (card == null) {
                response.put("success", false);
                response.put("message", "Carte non trouvée");
                return ResponseEntity. status(HttpStatus. NOT_FOUND). body(response);
            }

            if (! card.getAccount().getUser().getId().equals(user. getId())) {
                response.put("success", false);
                response.put("message", "Carte non autorisée");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if (card.isBlocked()) {
                response.put("success", false);
                response.put("message", "Carte bloquée: " + card.getBlockReason());
                response.put("cardBlocked", true);
                return ResponseEntity. status(HttpStatus. FORBIDDEN).body(response);
            }

            if (! passwordEncoder.matches(request.getPin(), card.getPinHash())) {
                boolean shouldBlock = card.incrementFailedAttempts();

                if (shouldBlock) {
                    card.blockCard("Trop de tentatives PIN incorrectes");
                }
                cardRepository.save(card);

                response. put("success", false);
                response. put("message", "Code PIN incorrect");
                response.put("remainingAttempts", card.getRemainingAttempts());
                response.put("cardBlocked", shouldBlock);

                return ResponseEntity. status(shouldBlock ? HttpStatus. FORBIDDEN : HttpStatus. UNAUTHORIZED).body(response);
            }

            card.resetFailedAttempts();
            cardRepository.save(card);

            response.put("success", true);
            response.put("message", "Code PIN valide");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la vérification du PIN: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{cardId}/status")
    public ResponseEntity<Map<String, Object>> getCardStatus(@PathVariable Long cardId) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response. put("success", false);
                response. put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus. UNAUTHORIZED).body(response);
            }

            BankCard card = cardRepository.findById(cardId). orElse(null);

            if (card == null) {
                response.put("success", false);
                response.put("message", "Carte non trouvée");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!card. getAccount().getUser().getId().equals(user.getId())) {
                response. put("success", false);
                response. put("message", "Carte non autorisée");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.put("success", true);
            response.put("id", card.getId());
            response.put("cardType", card.getCardType(). name());
            response.put("active", card.isActive());
            response. put("blocked", card.isBlocked());
            response.put("blockReason", card. getBlockReason());
            response.put("failedPinAttempts", card.getFailedPinAttempts());
            response. put("remainingAttempts", card.getRemainingAttempts());
            response.put("singleTransactionLimit", card.getSingleTransactionLimit());
            response.put("dailyTransactionLimit", card.getDailyTransactionLimit());
            response.put("maxDailyTransactions", card.getMaxDailyTransactions());

            return ResponseEntity. ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération du statut: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ Effectuer une transaction avec enregistrement en base et email de confirmation
     */
    @PostMapping("/transaction")
    public ResponseEntity<Map<String, Object>> processTransaction(
            @Valid @RequestBody CardTransactionRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            BankCard card = cardRepository. findById(request. getCardId()).orElse(null);

            if (card == null) {
                response.put("success", false);
                response.put("message", "Carte non trouvée");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!card.getAccount().getUser(). getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Carte non autorisée");
                return ResponseEntity.status(HttpStatus.FORBIDDEN). body(response);
            }

            if (card.isBlocked()) {
                response.put("success", false);
                response.put("errorType", "CARD_BLOCKED");
                response.put("message", "Carte bloquée: " + card.getBlockReason());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Vérifier le PIN
            if (!passwordEncoder.matches(request.getPin(), card.getPinHash())) {
                boolean shouldBlock = card. incrementFailedAttempts();

                if (shouldBlock) {
                    card. blockCard("Trop de tentatives PIN incorrectes");
                }
                cardRepository.save(card);

                response. put("success", false);
                response. put("errorType", "INVALID_PIN");
                response.put("message", "Code PIN incorrect");
                response.put("remainingAttempts", card.getRemainingAttempts());
                response.put("cardBlocked", shouldBlock);

                return ResponseEntity.status(shouldBlock ? HttpStatus. FORBIDDEN : HttpStatus.UNAUTHORIZED).body(response);
            }

            // PIN correct
            card.resetFailedAttempts();
            card.resetDailyCountersIfNeeded();

            BigDecimal amount = request.getAmount();

            // Vérifier les limites
            if (card.getSingleTransactionLimit() != null &&
                    amount. compareTo(card. getSingleTransactionLimit()) > 0) {
                response. put("success", false);
                response. put("errorType", "LIMIT_EXCEEDED");
                response.put("message", "Montant dépasse la limite par transaction: " + card.getSingleTransactionLimit() + " MAD");
                return ResponseEntity.badRequest().body(response);
            }

            if (card. getMaxDailyTransactions() != null &&
                    card.getDailyTransactionCount() >= card.getMaxDailyTransactions()) {
                response.put("success", false);
                response.put("errorType", "LIMIT_EXCEEDED");
                response.put("message", "Nombre maximum de transactions journalières atteint");
                return ResponseEntity.badRequest(). body(response);
            }

            if (card.getDailyTransactionLimit() != null) {
                BigDecimal newTotal = card.getDailyTransactionTotal().add(amount);
                if (newTotal.compareTo(card.getDailyTransactionLimit()) > 0) {
                    response.put("success", false);
                    response.put("errorType", "LIMIT_EXCEEDED");
                    response.put("message", "Limite journalière dépassée.  Reste: " +
                            card.getDailyTransactionLimit(). subtract(card.getDailyTransactionTotal()) + " MAD");
                    return ResponseEntity.badRequest(). body(response);
                }
            }

            // Vérifier le solde pour les débits
            BankAccount account = card.getAccount();
            if ("debit".equalsIgnoreCase(request.getType())) {
                if (account.getBalance().compareTo(amount) < 0) {
                    response.put("success", false);
                    response.put("message", "Solde insuffisant.  Disponible: " + account.getBalance() + " MAD");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // ✅ Stocker l'ancien solde AVANT modification
            BigDecimal oldBalance = account. getBalance();

            // Mettre à jour le solde
            if ("credit".equalsIgnoreCase(request. getType())) {
                account.setBalance(account.getBalance().add(amount));
            } else {
                account.setBalance(account.getBalance(). subtract(amount));
            }

            // ✅ Nouveau solde APRÈS modification
            BigDecimal newBalance = account. getBalance();

            // ✅ Générer référence et date
            String referenceNumber = "TXN" + System.currentTimeMillis();
            LocalDateTime transactionDate = LocalDateTime.now();

            // ✅ ENREGISTRER LA TRANSACTION EN BASE DE DONNÉES
            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction. setType(request.getType(). toLowerCase());
            transaction. setAmount(amount);
            transaction.setDescription(request.getDescription() != null ? request. getDescription() : "Transaction par carte");
            transaction. setReference(referenceNumber);
            transaction.setDate(transactionDate);
            transactionRepository.save(transaction);

            // Mettre à jour les compteurs de la carte
            card. setDailyTransactionCount(card.getDailyTransactionCount() + 1);
            card.setDailyTransactionTotal(card.getDailyTransactionTotal().add(amount));
            card.setLastTransactionDate(java.time.LocalDate.now());

            // Sauvegarder
            bankAccountRepository.save(account);
            cardRepository.save(card);

            // ✅ ENVOYER L'EMAIL DE CONFIRMATION
            try {
                emailService.sendTransactionConfirmationEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        user. getLastName(),
                        request.getType(),
                        amount,
                        referenceNumber,
                        transactionDate,
                        request.getDescription(),
                        card.getCardType().getDisplayName(),
                        card.getId(),
                        account.getAccountNumber(),
                        oldBalance,
                        newBalance
                );
            } catch (Exception emailError) {
                System.err.println("⚠️ Erreur envoi email de confirmation: " + emailError.getMessage());
            }

            // Réponse de succès
            response.put("success", true);
            response.put("message", "Transaction effectuée avec succès");
            response. put("transactionId", transaction. getId());
            response.put("referenceNumber", referenceNumber);
            response. put("amount", amount);
            response. put("type", request.getType());
            response.put("description", transaction.getDescription());
            response.put("oldBalance", oldBalance);
            response.put("newBalance", newBalance);
            response.put("transactionDate", transactionDate);
            response.put("remainingDailyLimit",
                    card. getDailyTransactionLimit() != null ?
                            card. getDailyTransactionLimit().subtract(card.getDailyTransactionTotal()) : null);
            response.put("remainingDailyTransactions",
                    card. getMaxDailyTransactions() != null ?
                            card. getMaxDailyTransactions() - card.getDailyTransactionCount() : null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors du traitement de la transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<Map<String, Object>> blockCard(
            @PathVariable Long cardId,
            @RequestBody(required = false) Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED). body(response);
            }

            BankCard card = cardRepository.findById(cardId).orElse(null);

            if (card == null || !card.getAccount().getUser().getId().equals(user. getId())) {
                response.put("success", false);
                response.put("message", "Carte non trouvée ou non autorisée");
                return ResponseEntity.status(HttpStatus. FORBIDDEN).body(response);
            }

            String reason = body != null ?  body.get("reason") : "Bloquée par l'utilisateur";
            card.blockCard(reason);
            cardRepository.save(card);

            response.put("success", true);
            response.put("message", "Carte bloquée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors du blocage de la carte");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockCard(@PathVariable Long cardId) {
        Map<String, Object> response = new HashMap<>();

        try {
            BankCard card = cardRepository.findById(cardId).orElse(null);

            if (card == null) {
                response.put("success", false);
                response.put("message", "Carte non trouvée");
                return ResponseEntity. status(HttpStatus. NOT_FOUND). body(response);
            }

            card.unblockCard();
            cardRepository.save(card);

            response.put("success", true);
            response.put("message", "Carte débloquée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response. put("success", false);
            response. put("message", "Erreur lors du déblocage de la carte");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR). body(response);
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
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Session expirée.  Veuillez vous reconnecter.");
                return ResponseEntity.status(401).body(response);
            }

            String rateLimitKey = user.getEmail() + ":" + clientIp;
            if (! rateLimitingService.isAllowed(rateLimitKey)) {
                response.put("success", false);
                response.put("message", "Trop de tentatives. Veuillez réessayer dans 15 minutes.");
                return ResponseEntity.status(429).body(response);
            }

            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", "Données invalides: " + bindingResult.getFieldError(). getDefaultMessage());
                return ResponseEntity. badRequest().body(response);
            }

            if (! passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                rateLimitingService. recordAttempt(rateLimitKey, false);
                response. put("success", false);
                response. put("message", "Mot de passe incorrect.");
                return ResponseEntity.status(403).body(response);
            }

            BankAccount account = bankAccountRepository.findById(request.getAccountId()). orElse(null);

            if (account == null || !account.getUser().getId().equals(user.getId())) {
                response. put("success", false);
                response. put("message", "Compte non autorisé.");
                return ResponseEntity.status(403). body(response);
            }

            CardValidationService.ValidationResult validation = cardValidationService. validateCardAddition(
                    request.getAccountId(), request.getCardType(), request.getPin());

            if (!validation.isValid()) {
                response.put("success", false);
                response.put("message", validation. getMessage());
                return ResponseEntity.badRequest().body(response);
            }

            BankCard newCard = bankAccountService.createCards(account,
                    new String[]{request.getCardType()},
                    new String[]{request. getPin()}). get(0);

            rateLimitingService. recordAttempt(rateLimitKey, true);

            response.put("success", true);
            response.put("message", "Carte ajoutée avec succès");
            response.put("cardType", newCard.getCardType().getDisplayName());

            return ResponseEntity. ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'ajout de la carte: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } finally {
            if (request != null) {
                request. clearSensitiveData();
            }
        }
    }

    private User getAuthenticatedUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            return userService.findByEmail(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
}