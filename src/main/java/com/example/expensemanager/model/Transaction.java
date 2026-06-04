package com.example.expensemanager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;   // Số tiền chi

    private String note;         // Ghi chú

    // Thời gian giao dịch — mặc định là thời điểm hiện tại
    // Nhưng user có thể chọn thời gian khác (chi hôm qua nhưng nhập hôm nay)
    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Nếu không truyền transactionDate → mặc định lấy thời gian hiện tại
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}