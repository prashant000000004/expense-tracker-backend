package com.aiexpensetracker.expense_tracker.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    @NotBlank(message = "Icon is required")
    private String icon;
    @NotBlank(message = "Color is required")
    private String color;

}
