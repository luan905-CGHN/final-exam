package com.example.expensemanager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nhiều ví thuộc về 1 user
    // FetchType.LAZY = chỉ load user khi cần, tiết kiệm tài nguyên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;           // Tên ví

    private String description;    // Mô tả

    // BigDecimal thay vì double vì tiền cần độ chính xác tuyệt đối
    // double có thể bị lỗi làm tròn: 0.1 + 0.2 = 0.30000000000000004
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;         // Số tiền hiện có

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDeposited = BigDecimal.ZERO;  // Tổng tiền đã nạp

    private String icon;           // Icon ví (emoji hoặc tên icon)

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}