package com.kakebo.repository;

import com.kakebo.entity.Category;
import com.kakebo.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE YEAR(e.date) = :year AND MONTH(e.date) = :month")
    List<Expense> findByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT e FROM Expense e WHERE e.category = :category AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    List<Expense> findByMonthAndCategory(@Param("year") int year, @Param("month") int month, @Param("category") Category category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal getTotalByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.category = :category AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal getTotalByCategoryAndMonth(@Param("year") int year, @Param("month") int month, @Param("category") Category category);
}
