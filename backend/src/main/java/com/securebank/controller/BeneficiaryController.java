package com.securebank.controller;

import com.securebank.dto. BeneficiaryRequestDto;
import com.securebank.dto.BeneficiaryResponseDto;
import com. securebank.model.User;
import com. securebank.service.BeneficiaryService;
import com.securebank. service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory. annotation.Autowired;
import org. springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context. SecurityContextHolder;
import org.springframework. validation. BindingResult;
import org.springframework.web. bind.annotation.*;

import java.util. HashMap;
import java. util.List;
import java.util. Map;

@RestController
@RequestMapping("/api/beneficiaries")
@CrossOrigin(origins = "http://localhost:3000")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final UserService userService;

    @Autowired
    public BeneficiaryController(BeneficiaryService beneficiaryService, UserService userService) {
        this. beneficiaryService = beneficiaryService;
        this.userService = userService;
    }

    /**
     * Get all beneficiaries for the authenticated user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBeneficiaries() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response. put("success", false);
                response.put("message", "Session expirée.  Veuillez vous reconnecter.");
                return ResponseEntity. status(401).body(response);
            }

            List<BeneficiaryResponseDto> beneficiaries = beneficiaryService.getBeneficiaries(user);

            response.put("success", true);
            response.put("beneficiaries", beneficiaries);
            response.put("count", beneficiaries. size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des bénéficiaires");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Add a new beneficiary
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addBeneficiary(
            @Valid @RequestBody BeneficiaryRequestDto request,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response. put("success", false);
                response. put("message", "Session expirée.  Veuillez vous reconnecter.");
                return ResponseEntity. status(401).body(response);
            }

            // Check validation errors
            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", bindingResult.getFieldErrors().get(0).getDefaultMessage());
                return ResponseEntity.badRequest().body(response);
            }

            BeneficiaryResponseDto beneficiary = beneficiaryService.addBeneficiary(user, request);

            response.put("success", true);
            response.put("message", "Bénéficiaire ajouté avec succès");
            response.put("beneficiary", beneficiary);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e. getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'ajout du bénéficiaire");
            return ResponseEntity.status(500). body(response);
        }
    }

    /**
     * Update a beneficiary
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBeneficiary(
            @PathVariable Long id,
            @Valid @RequestBody BeneficiaryRequestDto request,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Session expirée. Veuillez vous reconnecter.");
                return ResponseEntity.status(401). body(response);
            }

            if (bindingResult. hasErrors()) {
                response.put("success", false);
                response.put("message", bindingResult.getFieldErrors(). get(0).getDefaultMessage());
                return ResponseEntity.badRequest().body(response);
            }

            BeneficiaryResponseDto beneficiary = beneficiaryService.updateBeneficiary(user, id, request);

            response.put("success", true);
            response.put("message", "Bénéficiaire mis à jour avec succès");
            response.put("beneficiary", beneficiary);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e. getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour du bénéficiaire");
            return ResponseEntity.status(500). body(response);
        }
    }

    /**
     * Delete (deactivate) a beneficiary
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBeneficiary(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                response. put("success", false);
                response. put("message", "Session expirée.  Veuillez vous reconnecter.");
                return ResponseEntity. status(401).body(response);
            }

            beneficiaryService. removeBeneficiary(user, id);

            response.put("success", true);
            response.put("message", "Bénéficiaire supprimé avec succès");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e. getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression du bénéficiaire");
            return ResponseEntity. status(500).body(response);
        }
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder. getContext().getAuthentication();
        if (auth == null || !auth. isAuthenticated()) {
            return null;
        }
        return userService.findByEmail(auth.getName());
    }
}