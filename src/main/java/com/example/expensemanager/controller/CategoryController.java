package com.example.expensemanager.controller;

import com.example.expensemanager.dto.CategoryRequest;
import com.example.expensemanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // GET /api/categories — xem danh sách (ID 8)
    @GetMapping
    public ResponseEntity<?> getCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                categoryService.getCategories(userDetails.getUsername())
        );
    }

    // POST /api/categories — thêm danh mục (ID 9)
    @PostMapping
    public ResponseEntity<?> createCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.status(201)
                    .body(categoryService.createCategory(userDetails.getUsername(), request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/categories/{id} — sửa danh mục (ID 10)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.ok(
                    categoryService.updateCategory(userDetails.getUsername(), id, request)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}