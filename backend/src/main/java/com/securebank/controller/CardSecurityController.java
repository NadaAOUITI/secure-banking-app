package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.exception.*;
import com.securebank.service. CardSecurityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory. annotation.Autowired;
import org. springframework.http.HttpStatus;
import org.springframework.http. ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java. util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardSecurityController {

    private final CardSecurityService cardSecurityService;

    @Autowired
    public CardSecurityController(CardSecurityService cardSecurityService) {
        this.cardSecurityService = cardSecurityService;
    }

    /**
     * Vérifier le code PIN
     */
    @PostMapping("/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyPin(
            @Valid @RequestBody PinVerificationRequest request,
            Authentication auth) {

        Long userId = getUserId(auth);
        Map<String, Object> response = new HashMap<>();

        try {
            cardSecurityService.verifyPin(request.getCardId(), userId, request.getPin());
            response.put("success", true);
            response.put("message", "Code PIN valide");
            return ResponseEntity.ok(response);

        } catch (InvalidPinException e) {
            response. put("success", false);
            response. put("message", e.getMessage());
            response.put("remainingAttempts", e.getRemainingAttempts());
            response.put("cardBlocked", e. isCardBlocked());
            return ResponseEntity.status(e.isCardBlocked() ?  HttpStatus.FORBIDDEN : HttpStatus. UNAUTHORIZED).body(response);

        } catch (CardBlockedException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("cardBlocked", true);
            response.put("reason", e.getReason());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Effectuer une transaction
     */
    @PostMapping("/transaction")
    public ResponseEntity<Map<String, Object>> processTransaction(
            @Valid @RequestBody CardTransactionRequest request,
            Authentication auth) {

        Long userId = getUserId(auth);
        Map<String, Object> response = new HashMap<>();

        try {
            TransactionResponse result = cardSecurityService.processTransaction(userId, request);
            response. put("success", true);
            response. put("message", result.getMessage());
            response.put("referenceNumber", result. getReferenceNumber());
            response.put("amount", result. getAmount());
            response.put("transactionDate", result. getTransactionDate());
            response.put("remainingDailyLimit", result. getRemainingDailyLimit());
            response.put("remainingDailyTransactions", result.getRemainingDailyTransactions());
            return ResponseEntity.ok(response);

        } catch (InvalidPinException e) {
            response.put("success", false);
            response.put("errorType", "INVALID_PIN");
            response.put("message", e. getMessage());
            response.put("remainingAttempts", e.getRemainingAttempts());
            response.put("cardBlocked", e.isCardBlocked());
            return ResponseEntity. status(e.isCardBlocked() ?  HttpStatus.FORBIDDEN : HttpStatus. UNAUTHORIZED).body(response);

        } catch (CardBlockedException e) {
            response.put("success", false);
            response.put("errorType", "CARD_BLOCKED");
            response.put("message", e.getMessage());
            response. put("reason", e.getReason());
            return ResponseEntity.status(HttpStatus.FORBIDDEN). body(response);

        } catch (TransactionLimitException e) {
            response.put("success", false);
            response.put("errorType", "LIMIT_EXCEEDED");
            response.put("limitType", e.getLimitType().name());
            response. put("message", e.getMessage());
            response.put("requested", e.getRequested());
            response.put("limit", e.getLimit());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Obtenir le statut d'une carte
     */
    @GetMapping("/{cardId}/status")
    public ResponseEntity<CardStatusResponse> getCardStatus(
            @PathVariable Long cardId,
            Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(cardSecurityService.getCardStatus(cardId, userId));
    }

    /**
     * Bloquer sa carte
     */
    @PostMapping("/{cardId}/block")
    public ResponseEntity<Map<String, Object>> blockCard(
            @PathVariable Long cardId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {

        Long userId = getUserId(auth);
        String reason = body != null ?  body.get("reason") : null;
        cardSecurityService.blockCard(cardId, userId, reason);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Carte bloquée avec succès");
        return ResponseEntity.ok(response);
    }

    /**
     * Débloquer une carte (admin)
     */
    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockCard(@PathVariable Long cardId) {
        cardSecurityService.unblockCard(cardId);

        Map<String, Object> response = new HashMap<>();
        response. put("success", true);
        response. put("message", "Carte débloquée avec succès");
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour les limites
     */
    @PutMapping("/{cardId}/limits")
    public ResponseEntity<Map<String, Object>> updateLimits(
            @PathVariable Long cardId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Long userId = getUserId(auth);

        BigDecimal singleLimit = body.containsKey("singleTransactionLimit")
                ? new BigDecimal(body.get("singleTransactionLimit"). toString()) : null;
        BigDecimal dailyLimit = body.containsKey("dailyTransactionLimit")
                ? new BigDecimal(body.get("dailyTransactionLimit").toString()) : null;
        Integer maxTx = body.containsKey("maxDailyTransactions")
                ? Integer.parseInt(body. get("maxDailyTransactions").toString()) : null;

        cardSecurityService.updateLimits(cardId, userId, singleLimit, dailyLimit, maxTx);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Limites mises à jour");
        return ResponseEntity.ok(response);
    }

    private Long getUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}