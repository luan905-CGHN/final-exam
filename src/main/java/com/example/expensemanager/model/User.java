package com.example.expensemanager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data                    // Lombok tự tạo getter, setter, toString — không cần viết tay
@Entity                  // Báo Spring đây là bảng trong database
@Table(name = "users")   // Tên bảng trong PostgreSQL sẽ là "users"
public class User {

    @Id                                    // Đây là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Tự tăng: 1, 2, 3...
    private Long id;

    @Column(unique = true, nullable = false, length = 50)  // username không được trùng, không được null
    private String username;

    @Column(nullable = false)   // password bắt buộc có
    private String passwordHash;

    @Column(updatable = false)  // Chỉ set 1 lần khi tạo, không cho sửa
    private LocalDateTime createdAt;

    @Column(unique = true)
    private String email;

    @PrePersist   // Tự động chạy trước khi lưu vào DB
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}