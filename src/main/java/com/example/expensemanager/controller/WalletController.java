package com.example.expensemanager.controller;

import com.example.expensemanager.dto.DepositRequest;
import com.example.expensemanager.dto.WalletRequest;
import com.example.expensemanager.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // GET /api/wallets — lấy danh sách ví (ID 4)
    @GetMapping
    public ResponseEntity<?> getWallets(
            @AuthenticationPrincipal UserDetails userDetails) {
        // @AuthenticationPrincipal tự lấy user đang đăng nhập từ token
        return ResponseEntity.ok(walletService.getWallets(userDetails.getUsername()));
    }

    // GET /api/wallets/total — tổng tiền (ID 5)
    @GetMapping("/total")
    public ResponseEntity<?> getTotalBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                Map.of("totalBalance", walletService.getTotalBalance(userDetails.getUsername()))
        );
    }

    // POST /api/wallets — thêm ví (ID 18)
    @PostMapping
    public ResponseEntity<?> createWallet(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WalletRequest request) {
        try {
            return ResponseEntity.status(201)
                    .body(walletService.createWallet(userDetails.getUsername(), request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    // POST /api/wallets/{id}/deposit — nạp tiền (ID 7)
    @PostMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request) {
        try {
            return ResponseEntity.ok(
                    walletService.deposit(userDetails.getUsername(), id, request)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}