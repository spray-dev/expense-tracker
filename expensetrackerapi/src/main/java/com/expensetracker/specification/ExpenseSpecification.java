package com.expensetracker.specification;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;

public class ExpenseSpecification {
    public static Specification<Expense> hasUserId(Long userId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(
            root.get("user").get("id"),
            userId
        );
    }

    public static Specification<Expense> hasCategory(Category category) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(
                root.get("category"),
                category
            );
    }

    public static Specification<Expense> hasDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.between(
                root.get("date"),
                startDate,
                endDate
            );
    }

    public static Specification<Expense> hasDescriptionContainingIgnoreCase(String description) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")),
                "%" + description.toLowerCase() + "%"
            );
    }
}
