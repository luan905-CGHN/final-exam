package com.example.expensemanager.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private BigDecimal balance;
    private BigDecimal totalDeposited;
}