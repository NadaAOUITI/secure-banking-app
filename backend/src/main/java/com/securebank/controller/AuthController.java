package com.securebank.controller;

import com.securebank.dto.UserRegistrationDto;
import com.securebank.exception.PasswordMismatchException;
import com.securebank.exception.UserAlreadyExistsException;
import com.securebank.model.User;
import com.securebank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Endpoint d'inscription d'un nouvel utilisateur
     * @param registrationDto les données d'inscription
     * @param bindingResult résultat de la validation
     * @return ResponseEntity avec le résultat de l'inscription
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier les erreurs de validation
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage(),
                                (existing, replacement) -> existing + "; " + replacement
                        ));
                
                response.put("success", false);
                response.put("message", "Erreurs de validation");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Enregistrer l'utilisateur
            User user = userService.registerUser(registrationDto);
            
            // Réponse de succès (sans exposer le mot de passe)
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("createdAt", user.getCreatedAt());
            
            response.put("success", true);
            response.put("message", "Inscription réussie");
            response.put("user", userData);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (UserAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            
        } catch (PasswordMismatchException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint pour vérifier si un email existe
     * @param email l'email à vérifier
     * @return ResponseEntity avec le résultat de la vérification
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        boolean exists = userService.emailExists(email);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
}