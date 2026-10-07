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

    // Exclusive next-midnight bounds avoid database timestamp rounding into the next day.
    public static Specification<Expense> hasDateInHalfOpenRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate),
            criteriaBuilder.lessThan(root.get("date"), endDate)
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
