package com.aiexpensetracker.expense_tracker.category.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private boolean isDefault;

}
