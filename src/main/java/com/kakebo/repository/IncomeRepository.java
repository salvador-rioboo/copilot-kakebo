package com.kakebo.repository;

import com.kakebo.entity.Income;
import com.kakebo.entity.IncomeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    @Query("SELECT i FROM Income i WHERE YEAR(i.date) = :year AND MONTH(i.date) = :month")
    List<Income> findByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT i FROM Income i WHERE i.type = :type AND YEAR(i.date) = :year AND MONTH(i.date) = :month")
    List<Income> findByMonthAndType(@Param("year") int year, @Param("month") int month, @Param("type") IncomeType type);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE YEAR(i.date) = :year AND MONTH(i.date) = :month")
    BigDecimal getTotalByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.type = :type AND YEAR(i.date) = :year AND MONTH(i.date) = :month")
    BigDecimal getTotalByTypeAndMonth(@Param("year") int year, @Param("month") int month, @Param("type") IncomeType type);
}
