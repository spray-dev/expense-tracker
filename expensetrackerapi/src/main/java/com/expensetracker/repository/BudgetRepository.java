package com.expensetracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetracker.entity.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUser_IdAndYearAndMonth(Long userId, int year, int month);
    void deleteAllByUser_Id(Long userId);
}
