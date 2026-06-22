package com.aiexpensetracker.expense_tracker.category;

import com.aiexpensetracker.expense_tracker.category.dto.CategoryRequest;
import com.aiexpensetracker.expense_tracker.category.dto.CategoryResponse;
import com.aiexpensetracker.expense_tracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ─── Get Current User ─────────────────────────────────
    // every method calls this — gets user from JWT token
    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // ─── Get All Categories ───────────────────────────────
    public List<CategoryResponse> getCategories() {
        return categoryRepository
                .findByUserId(getCurrentUser().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Create Category ──────────────────────────────────
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();

        // business rule — no duplicate names per user
        if (categoryRepository.existsByNameAndUserId(
                request.getName(), user.getId())) {
            throw new RuntimeException(
                    "Category '" + request.getName() + "' already exists");
        }

        var category = Category.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .isDefault(false)   // user created = not default
                .user(user)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    // ─── Update Category ──────────────────────────────────
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        User user = getCurrentUser();

        // findByIdAndUserId ensures user can only edit THEIR categories
        var category = categoryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        // default categories cannot be edited
        if (category.isDefault()) {
            throw new RuntimeException("Cannot edit default categories");
        }

        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());

        return toResponse(categoryRepository.save(category));
    }

    // ─── Delete Category ──────────────────────────────────
    public void deleteCategory(Long id) {
        var category = categoryRepository
                .findByIdAndUserId(id, getCurrentUser().getId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        if (category.isDefault()) {
            throw new RuntimeException("Cannot delete default categories");
        }

        categoryRepository.delete(category);
    }

    // ─── Seed Default Categories ──────────────────────────
    // called once when user registers — creates default categories
    public void seedDefaultCategories(User user) {
        List<Category> defaults = List.of(
                buildDefault("Food", "🍔", "#FF5733", user),
                buildDefault("Transport", "🚗", "#3498DB", user),
                buildDefault("Bills", "💡", "#F39C12", user),
                buildDefault("Shopping", "🛍️", "#9B59B6", user),
                buildDefault("Health", "💊", "#2ECC71", user),
                buildDefault("Entertainment", "🎬", "#E74C3C", user),
                buildDefault("Education", "📚", "#1ABC9C", user),
                buildDefault("Other", "📦", "#95A5A6", user)
        );
        categoryRepository.saveAll(defaults);
    }

    private Category buildDefault(
            String name, String icon, String color, User user) {
        return Category.builder()
                .name(name)
                .icon(icon)
                .color(color)
                .isDefault(true)
                .user(user)
                .build();
    }

    // ─── Map Entity to Response ───────────────────────────
    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .isDefault(category.isDefault())
                .build();
    }
}