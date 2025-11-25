package com.securebank.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    // Critères de mot de passe fort
    private static final int MIN_LENGTH = 12;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Initialisation si nécessaire
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        
        boolean isValid = true;
        context.disableDefaultConstraintViolation();
        
        // Vérifier la longueur minimale
        if (password.length() < MIN_LENGTH) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères"
            ).addConstraintViolation();
            isValid = false;
        }
        
        // Vérifier la présence de majuscules
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins une lettre majuscule"
            ).addConstraintViolation();
            isValid = false;
        }
        
        // Vérifier la présence de minuscules
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins une lettre minuscule"
            ).addConstraintViolation();
            isValid = false;
        }
        
        // Vérifier la présence de chiffres
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins un chiffre"
            ).addConstraintViolation();
            isValid = false;
        }
        
        // Vérifier la présence de caractères spéciaux
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*()_+-=[]{}|;':\"\\,.<>/?)"
            ).addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
}