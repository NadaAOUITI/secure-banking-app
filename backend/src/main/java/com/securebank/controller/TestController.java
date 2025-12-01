package com.securebank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
public class TestController {
    
    @GetMapping("/")
    public Map<String, Object> testHttps() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "HTTPS is working!");
        response.put("secure", true);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}