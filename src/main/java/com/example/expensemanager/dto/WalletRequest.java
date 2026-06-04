package com.example.expensemanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WalletRequest {

    @NotBlank(message = "Tên ví không được để trống")
    private String name;

    private String description;

    private String icon;
}