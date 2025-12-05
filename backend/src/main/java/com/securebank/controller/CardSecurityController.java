package com.securebank.controller;

import com. securebank.dto.*;
import com.securebank. exception.*;
import com.securebank. model.User;
import com.securebank.service.CardSecurityService;
import com.securebank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http. ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "http://localhost:3000")
public class CardSecurityController {

    private final CardSecurityService cardSecurityService;
    private final UserService userService;

    @Autowired
    public CardSecurityController(CardSecurityService cardSecurityService, UserService userService) {
        this.cardSecurityService = cardSecurityService;
        this.userService = userService;
    }

    /**
     * Vérifier le code PIN
     */
    @PostMapping("/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyPin(
            @Valid @RequestBody PinVerificationRequest request,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            cardSecurityService.verifyPin(request.getCardId(), userId, request.getPin());
            response.put("success", true);
            response.put("message", "Code PIN valide");
            return ResponseEntity.ok(response);

        } catch (InvalidPinException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("remainingAttempts", e.getRemainingAttempts());
            response.put("cardBlocked", e.isCardBlocked());
            return ResponseEntity.status(e.isCardBlocked() ? HttpStatus.FORBIDDEN : HttpStatus. UNAUTHORIZED).body(response);

        } catch (CardBlockedException e) {
            response.put("success", false);
            response. put("message", e.getMessage());
            response.put("cardBlocked", true);
            response.put("reason", e.getReason());
            return ResponseEntity. status(HttpStatus.FORBIDDEN). body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la vérification du PIN");
            return ResponseEntity.status(HttpStatus. INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Effectuer une transaction
     */
    @PostMapping("/transaction")
    public ResponseEntity<Map<String, Object>> processTransaction(
            @Valid @RequestBody CardTransactionRequest request,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity. status(HttpStatus.UNAUTHORIZED). body(response);
            }

            TransactionResponse result = cardSecurityService.processTransaction(userId, request);
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("referenceNumber", result.getReferenceNumber());
            response.put("amount", result.getAmount());
            response.put("transactionDate", result.getTransactionDate());
            response.put("remainingDailyLimit", result.getRemainingDailyLimit());
            response.put("remainingDailyTransactions", result.getRemainingDailyTransactions());
            return ResponseEntity.ok(response);

        } catch (InvalidPinException e) {
            response.put("success", false);
            response.put("errorType", "INVALID_PIN");
            response.put("message", e.getMessage());
            response. put("remainingAttempts", e.getRemainingAttempts());
            response.put("cardBlocked", e.isCardBlocked());
            return ResponseEntity. status(e.isCardBlocked() ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED).body(response);

        } catch (CardBlockedException e) {
            response.put("success", false);
            response.put("errorType", "CARD_BLOCKED");
            response.put("message", e.getMessage());
            response.put("reason", e.getReason());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (TransactionLimitException e) {
            response.put("success", false);
            response.put("errorType", "LIMIT_EXCEEDED");
            response.put("limitType", e.getLimitType().name());
            response.put("message", e.getMessage());
            response. put("requested", e.getRequested());
            response.put("limit", e.getLimit());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors du traitement de la transaction");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR). body(response);
        }
    }

    /**
     * Obtenir le statut d'une carte
     */
    @GetMapping("/{cardId}/status")
    public ResponseEntity<? > getCardStatus(
            @PathVariable Long cardId,
            Authentication auth) {

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response. put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus. UNAUTHORIZED).body(response);
            }

            return ResponseEntity.ok(cardSecurityService.getCardStatus(cardId, userId));

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération du statut");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Bloquer sa carte
     */
    @PostMapping("/{cardId}/block")
    public ResponseEntity<Map<String, Object>> blockCard(
            @PathVariable Long cardId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String reason = body != null ? body. get("reason") : null;
            cardSecurityService.blockCard(cardId, userId, reason);

            response.put("success", true);
            response.put("message", "Carte bloquée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response. put("message", "Erreur lors du blocage de la carte");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Débloquer une carte (admin)
     */
    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockCard(@PathVariable Long cardId) {
        Map<String, Object> response = new HashMap<>();

        try {
            cardSecurityService.unblockCard(cardId);
            response.put("success", true);
            response.put("message", "Carte débloquée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors du déblocage de la carte");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR). body(response);
        }
    }

    /**
     * Mettre à jour les limites
     */
    @PutMapping("/{cardId}/limits")
    public ResponseEntity<Map<String, Object>> updateLimits(
            @PathVariable Long cardId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            BigDecimal singleLimit = body.containsKey("singleTransactionLimit")
                    ? new BigDecimal(body. get("singleTransactionLimit"). toString()) : null;
            BigDecimal dailyLimit = body.containsKey("dailyTransactionLimit")
                    ? new BigDecimal(body.get("dailyTransactionLimit").toString()) : null;
            Integer maxTx = body.containsKey("maxDailyTransactions")
                    ?  Integer.parseInt(body.get("maxDailyTransactions").toString()) : null;

            cardSecurityService.updateLimits(cardId, userId, singleLimit, dailyLimit, maxTx);

            response.put("success", true);
            response.put("message", "Limites mises à jour");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour des limites");
            return ResponseEntity.status(HttpStatus. INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user ID from authentication
     * Fixed: Now retrieves user by email and returns their ID
     */
    private Long getUserId(Authentication auth) {
        try {
            if (auth == null || ! auth.isAuthenticated()) {
                return null;
            }

            // Get email from authentication
            String email = auth.getName();

            // Find user by email
            User user = userService.findByEmail(email);

            return user != null ? user.getId() : null;

        } catch (Exception e) {
            return null;
        }
    }
}