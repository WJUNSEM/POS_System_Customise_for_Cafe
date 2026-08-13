package com.matchabox.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminAuthController {

    private static final String ADMIN_USERNAME = "MatchaBox@gmail.com";
    private static final String ADMIN_PASSWORD = "MatchaBox1234";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", ADMIN_USERNAME,
                    "message", "Admin login successful"
            ));
        }

        return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "error", "Invalid admin username or password"
        ));
    }
}
