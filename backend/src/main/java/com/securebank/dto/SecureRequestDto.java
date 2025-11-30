package com.securebank.dto;

import java.util.Arrays;

/**
 * Classe de base pour les DTOs contenant des données sensibles
 */
public abstract class SecureRequestDto {
    
    /**
     * Nettoie les données sensibles après utilisation
     */
    public void clearSensitiveData() {
        // Implémentation par défaut - à surcharger dans les classes filles
    }
    
    /**
     * Utilitaire pour nettoyer un tableau de chaînes
     */
    protected void clearStringArray(String[] array) {
        if (array != null) {
            Arrays.fill(array, null);
        }
    }
    
    /**
     * Utilitaire pour nettoyer une chaîne sensible
     */
    protected String clearString(String value) {
        return null; // Force la suppression de la référence
    }
}