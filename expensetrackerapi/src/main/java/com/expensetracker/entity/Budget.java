package com.expensetracker.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(
    name = "budgets",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "year", "month"})
    }
)
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Budget() {
        // JPA
    }

    public Budget(BigDecimal amount, int year, int month, User user) {
        this.amount = amount;
        this.year = year;
        this.month = month;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }   
}
