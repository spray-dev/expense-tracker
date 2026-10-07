package com.expensetracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.expensetracker.dto.expense.features.CategorySpendingSummary;
import com.expensetracker.dto.expense.features.DateRange;
import com.expensetracker.dto.expense.features.ExpenseMonthlyTotalPoint;
import com.expensetracker.dto.expense.features.ExpenseMonthlyTotalResponse;
import com.expensetracker.dto.expense.features.ExpenseYearlySummaryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.ExpensePeriod;
import com.expensetracker.entity.User;
import com.expensetracker.exception.InvalidResourceException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.specification.ExpenseSpecification;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;

@Service
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;

    private final UserService userService;
    
    public ExpenseService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    @Transactional 
    public Expense createExpense(String description, BigDecimal amount, LocalDateTime date, Category category, Long userId) {
        User user = userService.getUserById(userId);
        Expense expense = new Expense(description, amount, date, category, user);
        return expenseRepository.save(expense);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
            "Expense with id " + id + " does not exist"));
    }

    public Expense getExpenseByIdAndUserId(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId).orElseThrow(() -> new ResourceNotFoundException(
            "Expense with id " + id + " does not exist"));
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getExpensesByUserId(Long userId) {
        return expenseRepository.findAllByUser_Id(userId);
    }

    public List<Expense> getExpensesByUserIdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return expenseRepository.findAllByUser_IdAndDateBetween(userId, startDate, endDate);
    }

    public List<Expense> getExpensesByMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = LocalDateTime.of(yearMonth.atDay(1), LocalTime.MIN);
        LocalDateTime nextMonth = startOfMonth.plusMonths(1);
        
        return expenseRepository.findAllByUser_IdAndDateGreaterThanEqualAndDateLessThan(userId, startOfMonth, nextMonth);
    }

    //Features
    //1. Get Expenses by Month
    public BigDecimal getMonthlyTotal(Long userId, int year, int month) {
        List<Expense> expenses = getExpensesByMonth(userId, year, month);
        BigDecimal total = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            total = total.add(expense.getAmount());
        }
        return total;
    }

    //2. Get Monthly Summary by Category
    public Map<Category, BigDecimal> getMonthlyTotalsByCategory(Long userId, int year, int month) {
        List<Expense> expenses = getExpensesByMonth(userId, year, month);

        Map<Category, BigDecimal> categoryTotals = new HashMap<>();
        for (Expense expense : expenses) {
            Category expenseCategory = expense.getCategory();
            BigDecimal amount = expense.getAmount();

            BigDecimal existingAmount = categoryTotals.getOrDefault(expenseCategory, BigDecimal.ZERO);
            categoryTotals.put(expenseCategory, existingAmount.add(amount));
            
        }
        return categoryTotals;
    }
    
    //3. Get Monthly Totals over Time
    public List<ExpenseMonthlyTotalPoint> getMonthlyTotalsOverTime(Long userId, int startYear, int startMonth, int endYear, int endMonth) {
        YearMonth startYearMonth = YearMonth.of(startYear, startMonth);
        YearMonth endYearMonth = YearMonth.of(endYear, endMonth);
        if (startYearMonth.isAfter(endYearMonth)) {
            throw new InvalidResourceException("Start year and month must not be after end year and month.");
        }

        List<ExpenseMonthlyTotalPoint> monthlyTotals = new ArrayList<>();

        for (YearMonth current = startYearMonth; !current.isAfter(endYearMonth); current = current.plusMonths(1)) {
            BigDecimal monthlyTotal = getMonthlyTotal(userId, current.getYear(), current.getMonthValue());
            monthlyTotals.add(new ExpenseMonthlyTotalPoint(current.getYear(), current.getMonthValue(), monthlyTotal));
        }
        return monthlyTotals;
    }

    //4. Top Spending Categories
    public List<CategorySpendingSummary> getTopSpendingCategories(Long userId, int year, int month) {
        Map<Category, BigDecimal> categoryTotals = getMonthlyTotalsByCategory(userId, year, month);

        List<Map.Entry<Category, BigDecimal>> entries = new ArrayList<>(categoryTotals.entrySet());
        entries.sort(
            (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())
        );

        List<CategorySpendingSummary> topSpendingCategories = new ArrayList<>();
        for (Map.Entry<Category, BigDecimal> entry : entries) {
            topSpendingCategories.add(new CategorySpendingSummary(entry.getKey(), entry.getValue()));
        }

        return topSpendingCategories;
    }

    //5. Average Spending
    public BigDecimal getAverageSpending(Long userId, int year, int month) {
        List<Expense> expenses = getExpensesByMonth(userId, year, month);

        if (expenses.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = getMonthlyTotal(userId, year, month);

        return total.divide(BigDecimal.valueOf(expenses.size()), RoundingMode.HALF_UP);
    }

    //6. Count of Expenses
    public int getCountOfExpenses(Long userId, int year, int month) {
        List<Expense> expenses = getExpensesByMonth(userId, year, month);
        return expenses.size();
    }

    //7. Search By Description
    public Page<Expense> filterExpenses(
        Long userId, Category category, 
        LocalDateTime startDate, LocalDateTime endDate, 
        String description, 
        String sortBy, String direction, int page, int size, ExpensePeriod period) {

        Specification<Expense> spec = ExpenseSpecification.hasUserId(userId);
    
        if (category != null) {
            spec = spec.and(ExpenseSpecification.hasCategory(category));
        }
        
        if (description != null && !description.isBlank()) {
            spec = spec.and(ExpenseSpecification.hasDescriptionContainingIgnoreCase(description));
        }

        // Sorting
        String sortField = "date";

        if (sortBy != null && !sortBy.isBlank()) {
            sortField = sortBy.toLowerCase();
        }

        String sortDirection = "desc";

        if (direction != null && !direction.isBlank()) {
            sortDirection = direction.toLowerCase();
        }

        if (!sortField.equals("date") 
        && !sortField.equals("amount") 
        && !sortField.equals("description") ) {
            throw new InvalidResourceException("Invalid sort field: " + sortField);
        }

        if (!sortDirection.equals("asc") && !sortDirection.equals("desc")) {
            throw new InvalidResourceException("Invalid sort direction: " + sortDirection);
        }

        Sort sort = Sort.by(sortField);
        
        if ("desc".equalsIgnoreCase(sortDirection)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        // Filter by period
        if (period != null) {
            DateRange range = getDateRangeForPeriod(period);
            spec = spec.and(
                ExpenseSpecification.hasDateInHalfOpenRange(range.startDate(), range.endDate().plusNanos(1))
            ); 
        } else if (startDate != null && endDate != null) {
            spec = spec.and(
                ExpenseSpecification.hasDateBetween(startDate, endDate)
            );
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        return expenseRepository.findAll(spec, pageable);
    }

    //8. Get Recent Expenses
    public List<Expense> getRecentExpenses(Long userId, int limit) {
        Specification<Expense> spec = ExpenseSpecification.hasUserId(userId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("date").descending());
        return expenseRepository.findAll(spec, pageable).getContent();
    }

    //9. Get Largest Expenses
    public List<Expense> getLargestExpenses(Long userId, int limit) {
        Specification<Expense> spec = ExpenseSpecification.hasUserId(userId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("amount").descending());
        return expenseRepository.findAll(spec, pageable).getContent();
    }

    public List<Expense> getRecentExpenses(Long userId, int year, int month, int limit) {
        return getRankedExpensesByMonth(userId, year, month, limit, "date");
    }

    public List<Expense> getLargestExpenses(Long userId, int year, int month, int limit) {
        return getRankedExpensesByMonth(userId, year, month, limit, "amount");
    }

    private List<Expense> getRankedExpensesByMonth(Long userId, int year, int month, int limit, String sortField) {
        LocalDateTime start = YearMonth.of(year, month).atDay(1).atStartOfDay();
        Specification<Expense> spec = ExpenseSpecification.hasUserId(userId)
            .and(ExpenseSpecification.hasDateInHalfOpenRange(start, start.plusMonths(1)));
        Pageable pageable = PageRequest.of(0, limit, Sort.by(sortField).descending());
        return expenseRepository.findAll(spec, pageable).getContent();
    }

    //10. Yearly Summary
    public ExpenseYearlySummaryResponse getYearlySummary(Long userId, int year) {
        List<ExpenseMonthlyTotalResponse> monthlyTotals = new ArrayList<>();
        BigDecimal yearlyTotal = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            BigDecimal monthlyTotal = getMonthlyTotal(userId, year, month);

            monthlyTotals.add(
                new ExpenseMonthlyTotalResponse(
                    year,
                    month,
                    monthlyTotal
                )
            );

            yearlyTotal = yearlyTotal.add(monthlyTotal);
        }

        return new ExpenseYearlySummaryResponse(
            year,
            yearlyTotal,
            monthlyTotals
        );
    }

    //11. Date Range Helper Method
    private DateRange getDateRangeForPeriod(ExpensePeriod period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime endDate;

        switch (period) {
            case THIS_MONTH:
                startDate = now.withDayOfMonth(1).with(LocalTime.MIN);
                endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);
                break;
            case LAST_MONTH:
                LocalDateTime lastMonth = now.minusMonths(1);
                startDate = lastMonth.withDayOfMonth(1).with(LocalTime.MIN);
                endDate = lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);
                break;
            case LAST_7_DAYS:
                startDate = now.minusDays(6).with(LocalTime.MIN);
                endDate = now.with(LocalTime.MAX);
                break;
            case LAST_30_DAYS:
                startDate = now.minusDays(29).with(LocalTime.MIN);
                endDate = now.with(LocalTime.MAX);
                break;
            case THIS_YEAR:
                startDate = now.withDayOfYear(1).with(LocalTime.MIN);
                endDate = now.withDayOfYear(now.toLocalDate().lengthOfYear()).with(LocalTime.MAX);
                break;
            default:
                throw new InvalidResourceException("Invalid expense period: " + period);
        }

        return new DateRange(startDate, endDate);
    }

    // 12. CSV Export
    public String exportExpensesToCsv(List<Expense> expenses) {
        StringBuilder csv = new StringBuilder();

        csv.append("id,description,amount,date,category\n");

        for (Expense expense : expenses) {
            csv.append(expense.getId()).append(",");
            csv.append("\"").append(expense.getDescription().replace("\"", "\"\"")).append("\"").append(",");
            csv.append(expense.getAmount()).append(",");
            csv.append(expense.getDate()).append(",");
            csv.append(expense.getCategory()).append("\n");
        }

        return csv.toString();
    }

    @Transactional 
    public Expense updateExpense(Long id, Long userId, String description, BigDecimal amount, LocalDateTime date, Category category) {
        Expense expense = getExpenseByIdAndUserId(id, userId);
        // Only update the fields that are not null
        if (description != null) {
            if (description.isBlank()) {
                throw new InvalidResourceException("Description cannot be blank");
            }
            expense.setDescription(description);
        }
        if (amount != null) {
            expense.setAmount(amount);
        }
        if (date != null) {
            expense.setDate(date);
        }
        if (category != null) {
            expense.setCategory(category);
        }
        // transactionType and user remain unchanged
        return expense;
    }

    @Transactional
    public void deleteExpense(Long id, Long userId) {
        Expense expense = getExpenseByIdAndUserId(id, userId);
        expenseRepository.delete(expense);
    }
}
