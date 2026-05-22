package com.example.expensemanager.controller;

import com.example.expensemanager.dto.LoginRequest;
import com.example.expensemanager.dto.RegisterRequest;
import com.example.expensemanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(201)
                    .body(Map.of("message", "Đăng ký thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT stateless — server không lưu session
        // Frontend nhận response này → tự xoá token khỏi localStorage
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }
}