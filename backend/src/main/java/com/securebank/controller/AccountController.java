package com.securebank.controller;

import com.securebank.dto.AccountDetailsDto;
import com.securebank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des comptes bancaires
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Endpoint pour récupérer les détails du compte de l'utilisateur authentifié
     */
    @GetMapping("/details")
    public ResponseEntity<AccountDetailsDto> getAccountDetails() {
        try {
            AccountDetailsDto accountDetails = accountService.getAccountDetails();
            return ResponseEntity.ok(accountDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}