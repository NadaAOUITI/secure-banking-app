package com.securebank.controller;

import com.securebank.dto.AddAccountRequest;
import com.securebank.model.BankAccount;
import com.securebank.model.User;
import com.securebank.service.BankAccountService;
import com.securebank.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:3000")
public class BankAccountController {
    
    private final BankAccountService bankAccountService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public BankAccountController(BankAccountService bankAccountService, UserService userService, PasswordEncoder passwordEncoder) {
        this.bankAccountService = bankAccountService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAccount(
            @Valid @RequestBody AddAccountRequest request,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user from Spring Security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Session expirée. Veuillez vous reconnecter.");
                return ResponseEntity.status(401).body(response);
            }
            
            User user = userService.findByEmail(auth.getName());
            if (user == null) {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Verify password for sensitive operation
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "Mot de passe incorrect.");
                return ResponseEntity.status(403).body(response);
            }
            
            // Validate request data
            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", "Données invalides");
                response.put("errors", bindingResult.getAllErrors());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create new account using existing service
            BankAccount newAccount = bankAccountService.createAccount(
                user, 
                request.getAccountType(), 
                request.getInitialDeposit()
            );
            
            // Create cards if selected
            if (request.getSelectedCards() != null && request.getSelectedCards().length > 0) {
                bankAccountService.createCards(newAccount, request.getSelectedCards(), request.getCardPins());
            }
            
            response.put("success", true);
            response.put("message", "Compte et cartes créés avec succès");
            response.put("accountNumber", newAccount.getAccountNumber());
            response.put("accountType", newAccount.getAccountType().getDisplayName());
            response.put("balance", newAccount.getBalance());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la création du compte: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}