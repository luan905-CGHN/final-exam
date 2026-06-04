package com.example.expensemanager.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String categoryName;
    private String categoryIcon;
    private String walletName;
    private String note;
    private LocalDateTime transactionDate;
}