package com.example.expensemanager.dto;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String note;
    private String icon;
}