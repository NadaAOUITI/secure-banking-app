package com.securebank.service;

import com.securebank.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class SecureDataMaskingService {

    @Autowired
    private EncryptionUtil encryptionUtil;

    public String maskCardNumber(String encryptedCardNumber) {
        if (encryptedCardNumber == null) return "**** **** **** ****";
        
        try {
            char[] fullNumber = encryptionUtil.decrypt(encryptedCardNumber).toCharArray();
            if (fullNumber.length >= 16) {
                String masked = new String(fullNumber, 0, 4) + " **** **** " + new String(fullNumber, 12, 4);
                Arrays.fill(fullNumber, '\0');
                return masked;
            }
            Arrays.fill(fullNumber, '\0');
        } catch (Exception e) {
            // Fallback sécurisé
        }
        return "**** **** **** ****";
    }

    public String maskPhone(String encryptedPhone) {
        if (encryptedPhone == null) return "****";
        
        try {
            String phone = encryptionUtil.decrypt(encryptedPhone);
            if (phone.length() >= 8) {
                return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
            }
        } catch (Exception e) {
            // Fallback sécurisé
        }
        return "****";
    }

    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****@****.***";
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) return "**@" + domain;
        return localPart.substring(0, 2) + "****@" + domain;
    }

    public String decryptSafely(String encryptedData) {
        if (encryptedData == null) return null;
        
        try {
            return encryptionUtil.decrypt(encryptedData);
        } catch (Exception e) {
            return null;
        }
    }
}