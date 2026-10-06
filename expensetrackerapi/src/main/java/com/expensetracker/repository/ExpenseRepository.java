package com.expensetracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.expensetracker.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndUser_Id(Long id, Long userId);

    List<Expense> findAllByUser_Id(Long userId);

    List<Expense> findAllByUser_IdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
