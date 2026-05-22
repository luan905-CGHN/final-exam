package com.example.expensemanager.controller;

import com.example.expensemanager.dto.TransactionRequest;
import com.example.expensemanager.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // POST /api/transactions — thêm khoản chi (ID 11)
    @PostMapping
    public ResponseEntity<?> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.status(201)
                    .body(transactionService.createTransaction(
                            userDetails.getUsername(), request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/transactions/today — giao dịch hôm nay (ID 15)
    @GetMapping("/today")
    public ResponseEntity<?> getTodayTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        var transactions = transactionService
                .getTodayTransactions(userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
                "transactions", transactions,
                "total", transactionService.calculateTotal(transactions)
        ));
    }

    // GET /api/transactions?startDate=...&endDate=... — lọc theo ngày (ID 14)
    @GetMapping
    public ResponseEntity<?> getTransactionsByDateRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {
        var transactions = transactionService
                .getTransactionsByDateRange(userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "transactions", transactions,
                "total", transactionService.calculateTotal(transactions)
        ));
    }

    // GET /api/transactions/wallet/{walletId}?startDate=...&endDate=... (ID 16)
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<?> getTransactionsByWallet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {
        var transactions = transactionService.getTransactionsByWalletAndDateRange(
                userDetails.getUsername(), walletId, startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "transactions", transactions,
                "total", transactionService.calculateTotal(transactions)
        ));
    }

    // PUT /api/transactions/{id} — sửa khoản chi (ID 12)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.ok(transactionService
                    .updateTransaction(userDetails.getUsername(), id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/transactions/{id} — xoá khoản chi (ID 13)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            transactionService.deleteTransaction(userDetails.getUsername(), id);
            return ResponseEntity.ok(Map.of("message", "Xoá thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}