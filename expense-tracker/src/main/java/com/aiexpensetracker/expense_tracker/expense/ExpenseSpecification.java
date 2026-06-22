package com.aiexpensetracker.expense_tracker.expense;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ExpenseSpecification {

    public static Specification<Expense> belongsToUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Expense> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction(); // no filter
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Expense> dateBetween(
            LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) {
                return cb.between(root.get("date"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), from);
            }
            return cb.lessThanOrEqualTo(root.get("date"), to);
        };
    }

    public static Specification<Expense> titleContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + search.toLowerCase() + "%"
            );
        };
    }
}

