package com.example.expensemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor  // Lombok tự tạo constructor có tất cả tham số
public class AuthResponse {
    private String token;
    private String username;
    private String message;
}