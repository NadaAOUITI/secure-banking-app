package com.securebank. controller;

import com.securebank. dto.TransferRequestDto;
import com.securebank. dto.TransferResponseDto;
import com.securebank. model.User;
import com. securebank.service.TransferService;
import com.securebank. service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory. annotation.Autowired;
import org. springframework.format.annotation.DateTimeFormat;
import org.springframework.http. ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context. SecurityContextHolder;
import org.springframework. validation. BindingResult;
import org.springframework.web. bind.annotation.*;

import java.math. BigDecimal;
import java.time. LocalDateTime;
import java.util.HashMap;
import java. util.List;
import java.util. Map;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TransferController {

    private final TransferService transferService;
    private final UserService userService;

    @Autowired
    public TransferController(TransferService transferService, UserService userService) {
        this. transferService = transferService;
        this. userService = userService;
    }

    /**
     * Initiate a new transfer (Step 1 - sends OTP)
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiateTransfer(
            @Valid @RequestBody TransferRequestDto request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== TRANSFER INITIATE REQUEST ===");
            System.out.println("SenderAccountId: " + request. getSenderAccountId());
            System.out.println("BeneficiaryId: " + request. getBeneficiaryId());
            System.out.println("Amount: " + request.getAmount());

            User user = getAuthenticatedUser();
            if (user == null) {
                System.out.println("ERROR: User not authenticated");
                response.put("success", false);
                response.put("message", "Session expirée. Veuillez vous reconnecter.");
                return ResponseEntity.status(401).body(response);
            }

            System.out.println("User: " + user. getEmail());

            if (bindingResult. hasErrors()) {
                String errorMsg = bindingResult. getFieldErrors().get(0).getDefaultMessage();
                System.out.println("Validation error: " + errorMsg);
                response. put("success", false);
                response. put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }

            TransferResponseDto transfer = transferService.initiateTransfer(user, request, httpRequest);

            System.out. println("Transfer initiated successfully: " + transfer.getReference());

            response.put("success", true);
            response.put("message", "Code OTP envoyé par email. Veuillez confirmer le virement.");
            response.put("transfer", transfer);
            response.put("requiresOtp", true);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("ERROR in initiateTransfer: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Confirm transfer with OTP (Step 2)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmTransfer(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out. println("=== TRANSFER CONFIRM REQUEST ===");

            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Session expirée. Veuillez vous reconnecter.");
                return ResponseEntity.status(401). body(response);
            }

            String reference = request.get("reference");
            String otpCode = request. get("otpCode");

            System.out.println("Reference: " + reference);
            System. out.println("OTP Code: " + (otpCode != null ? "****" : "null"));

            if (reference == null || reference.isEmpty()) {
                response.put("success", false);
                response.put("message", "Référence du virement manquante");
                return ResponseEntity.badRequest().body(response);
            }

            if (otpCode == null || otpCode. length() != 6) {
                response. put("success", false);
                response. put("message", "Code OTP invalide (6 chiffres requis)");
                return ResponseEntity.badRequest().body(response);
            }

            TransferResponseDto transfer = transferService. confirmTransfer(user, reference, otpCode, httpRequest);

            System.out.println("Transfer confirmed successfully");

            response.put("success", true);
            response.put("message", "Virement effectué avec succès!");
            response.put("transfer", transfer);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("ERROR in confirmTransfer: " + e. getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e. getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Cancel a pending transfer
     */
    @PostMapping("/{reference}/cancel")
    public ResponseEntity<Map<String, Object>> cancelTransfer(@PathVariable String reference) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Session expirée.");
                return ResponseEntity.status(401). body(response);
            }

            transferService.cancelTransfer(user, reference);

            response.put("success", true);
            response.put("message", "Virement annulé");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity. badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get transfer history
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getTransferHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat. ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO. DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Long beneficiaryId) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Session expirée.");
                return ResponseEntity.status(401).body(response);
            }

            List<TransferResponseDto> transfers = transferService.getTransferHistory(
                    user, startDate, endDate, minAmount, maxAmount, beneficiaryId
            );

            response.put("success", true);
            response.put("transfers", transfers);
            response.put("count", transfers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("ERROR in getTransferHistory: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            response.put("transfers", List.of());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get single transfer details
     */
    @GetMapping("/{reference}")
    public ResponseEntity<Map<String, Object>> getTransfer(@PathVariable String reference) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response. put("success", false);
                response. put("message", "Session expirée.");
                return ResponseEntity.status(401). body(response);
            }

            return transferService.getTransfer(user, reference)
                    .map(transfer -> {
                        response.put("success", true);
                        response. put("transfer", transfer);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("success", false);
                        response.put("message", "Virement non trouvé");
                        return ResponseEntity.status(404).body(response);
                    });

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userService. findByEmail(auth.getName());
    }
}