package com.securebank.controller;

import com.securebank.dto.TransactionDTO;
import com.securebank.model.Transaction;
import com.securebank.model.User;
import com.securebank.service.TransactionService;
import com.securebank.service.UserService;
import org. springframework.beans.factory.annotation. Autowired;
import org. springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework. security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transaction")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    /**
     * Récupérer toutes les transactions de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<? > getUserTransactions(Authentication auth) {
        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response. put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus. UNAUTHORIZED).body(response);
            }

            // Récupérer toutes les transactions de l'utilisateur
            List<TransactionDTO> transactions = transactionService.getTransactionsByUser(userId);

            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des transactions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR). body(response);
        }
    }

    /**
     * Récupérer les transactions d'un compte spécifique
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<?> getAccountTransactions(
            @PathVariable Long accountId,
            Authentication auth) {

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<TransactionDTO> transactions = transactionService.getTransactionsByAccountAndUser(accountId, userId);

            return ResponseEntity.ok(transactions);

        } catch (SecurityException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des transactions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Récupérer une transaction spécifique
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(
            @PathVariable Long transactionId,
            Authentication auth) {

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            TransactionDTO transaction = transactionService.getTransactionById(transactionId, userId);

            return ResponseEntity. ok(transaction);

        } catch (SecurityException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Transaction non trouvée");
            return ResponseEntity.status(HttpStatus. NOT_FOUND).body(response);
        }
    }

    /**
     * Récupérer les statistiques des transactions
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getTransactionStatistics(Authentication auth) {
        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus. UNAUTHORIZED).body(response);
            }

            Map<String, Object> statistics = transactionService.getTransactionStatistics(userId);

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des statistiques");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user ID from authentication
     */
    private Long getUserId(Authentication auth) {
        try {
            if (auth == null || ! auth.isAuthenticated()) {
                return null;
            }

            String email = auth.getName();
            User user = userService.findByEmail(email);

            return user != null ? user. getId() : null;

        } catch (Exception e) {
            return null;
        }
    }
    /**
     * Créer une nouvelle transaction
     */
    @PostMapping
    public ResponseEntity<? > createTransaction(
            @RequestBody Map<String, Object> request,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserId(auth);
            if (userId == null) {
                response. put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED). body(response);
            }

            // Extraire les données de la requête
            Long accountId = Long.parseLong(request.get("accountId").toString());
            String type = request.get("type"). toString();
            BigDecimal amount = new BigDecimal(request.get("amount"). toString());
            String description = request.get("description").toString();
            String reference = request.get("reference") != null ? request. get("reference").toString() : null;

            // Valider les données
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                response. put("success", false);
                response. put("message", "Le montant doit être supérieur à 0");
                return ResponseEntity.badRequest().body(response);
            }

            if (! type.equals("credit") && !type. equals("debit")) {
                response. put("success", false);
                response. put("message", "Type de transaction invalide");
                return ResponseEntity.badRequest().body(response);
            }

            // Créer la transaction
            Transaction transaction = transactionService.createTransaction(
                    accountId, userId, type, amount, description, reference
            );

            response.put("success", true);
            response.put("message", "Transaction effectuée avec succès");
            response.put("transactionId", transaction. getId());
            response.put("date", transaction.getDate());

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus. FORBIDDEN).body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la transaction");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR). body(response);
        }
    }
}