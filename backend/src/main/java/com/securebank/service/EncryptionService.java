package com.securebank.service;

import com.securebank.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    
    private final EncryptionUtil encryptionUtil;
    
    @Autowired
    public EncryptionService(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }
    
    public String encryptSensitiveData(String data) {
        return encryptionUtil.encrypt(data);
    }
    
    public String decryptSensitiveData(String encryptedData) {
        return encryptionUtil.decrypt(encryptedData);
    }
    
    public String maskEmailForDisplay(String email) {
        return encryptionUtil.maskEmail(email);
    }
    
    public String maskPhoneForDisplay(String phone) {
        return encryptionUtil.maskPhone(phone);
    }
}