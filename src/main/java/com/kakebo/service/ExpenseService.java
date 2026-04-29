package com.kakebo.service;

import com.kakebo.dto.CreateExpenseDTO;
import com.kakebo.dto.ExpenseResponseDTO;
import com.kakebo.entity.Category;
import com.kakebo.entity.Expense;
import com.kakebo.exception.InvalidExpenseException;
import com.kakebo.exception.ResourceNotFoundException;
import com.kakebo.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponseDTO create(CreateExpenseDTO dto) {
        validateExpenseDTO(dto);

        Expense expense = new Expense(
            dto.description(),
            dto.amount(),
            dto.category(),
            dto.isFixed(),
            dto.isRecurring(),
            dto.date()
        );

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponseDTO.from(saved);
    }

    public ExpenseResponseDTO getById(Long id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return ExpenseResponseDTO.from(expense);
    }

    public List<ExpenseResponseDTO> getAllByMonth(int year, int month) {
        return expenseRepository.findByMonth(year, month)
            .stream()
            .map(ExpenseResponseDTO::from)
            .collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> getByMonthAndCategory(int year, int month, Category category) {
        return expenseRepository.findByMonthAndCategory(year, month, category)
            .stream()
            .map(ExpenseResponseDTO::from)
            .collect(Collectors.toList());
    }

    public BigDecimal getTotalByMonth(int year, int month) {
        return expenseRepository.getTotalByMonth(year, month);
    }

    public BigDecimal getTotalByMonthAndCategory(int year, int month, Category category) {
        return expenseRepository.getTotalByCategoryAndMonth(year, month, category);
    }

    public Map<Category, BigDecimal> getTotalsByCategory(int year, int month) {
        return expenseRepository.findByMonth(year, month)
            .stream()
            .collect(Collectors.groupingBy(
                Expense::getCategory,
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));
    }

    @Transactional
    public ExpenseResponseDTO update(Long id, CreateExpenseDTO dto) {
        validateExpenseDTO(dto);

        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setCategory(dto.category());
        expense.setFixed(dto.isFixed());
        expense.setRecurring(dto.isRecurring());
        expense.setDate(dto.date());

        Expense updated = expenseRepository.save(expense);
        return ExpenseResponseDTO.from(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    private void validateExpenseDTO(CreateExpenseDTO dto) {
        if (dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException("Amount must be positive");
        }
        if (dto.description() == null || dto.description().isBlank()) {
            throw new InvalidExpenseException("Description is required");
        }
        if (dto.category() == null) {
            throw new InvalidExpenseException("Category is required");
        }
        if (dto.date() == null) {
            throw new InvalidExpenseException("Date is required");
        }
        if (dto.date().isAfter(LocalDate.now())) {
            throw new InvalidExpenseException("Date cannot be in the future");
        }
    }
}
