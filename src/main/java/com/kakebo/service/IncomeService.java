package com.kakebo.service;

import com.kakebo.dto.CreateIncomeDTO;
import com.kakebo.dto.IncomeResponseDTO;
import com.kakebo.entity.Income;
import com.kakebo.entity.IncomeType;
import com.kakebo.exception.InvalidIncomeException;
import com.kakebo.exception.ResourceNotFoundException;
import com.kakebo.repository.IncomeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncomeService {
    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Transactional
    public IncomeResponseDTO create(CreateIncomeDTO dto) {
        validateIncomeDTO(dto);

        Income income = new Income(
            dto.description(),
            dto.amount(),
            dto.type(),
            dto.isRecurring(),
            dto.date()
        );

        Income saved = incomeRepository.save(income);
        return IncomeResponseDTO.from(saved);
    }

    public IncomeResponseDTO getById(Long id) {
        Income income = incomeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));
        return IncomeResponseDTO.from(income);
    }

    public List<IncomeResponseDTO> getAllByMonth(int year, int month) {
        return incomeRepository.findByMonth(year, month)
            .stream()
            .map(IncomeResponseDTO::from)
            .collect(Collectors.toList());
    }

    public List<IncomeResponseDTO> getByMonthAndType(int year, int month, IncomeType type) {
        return incomeRepository.findByMonthAndType(year, month, type)
            .stream()
            .map(IncomeResponseDTO::from)
            .collect(Collectors.toList());
    }

    public BigDecimal getTotalByMonth(int year, int month) {
        return incomeRepository.getTotalByMonth(year, month);
    }

    public BigDecimal getTotalByMonthAndType(int year, int month, IncomeType type) {
        return incomeRepository.getTotalByTypeAndMonth(year, month, type);
    }

    @Transactional
    public IncomeResponseDTO update(Long id, CreateIncomeDTO dto) {
        validateIncomeDTO(dto);

        Income income = incomeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));

        income.setDescription(dto.description());
        income.setAmount(dto.amount());
        income.setType(dto.type());
        income.setRecurring(dto.isRecurring());
        income.setDate(dto.date());

        Income updated = incomeRepository.save(income);
        return IncomeResponseDTO.from(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!incomeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Income not found with id: " + id);
        }
        incomeRepository.deleteById(id);
    }

    private void validateIncomeDTO(CreateIncomeDTO dto) {
        if (dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidIncomeException("Amount must be positive");
        }
        if (dto.description() == null || dto.description().isBlank()) {
            throw new InvalidIncomeException("Description is required");
        }
        if (dto.type() == null) {
            throw new InvalidIncomeException("Income type is required");
        }
        if (dto.date() == null) {
            throw new InvalidIncomeException("Date is required");
        }
        if (dto.date().isAfter(LocalDate.now())) {
            throw new InvalidIncomeException("Date cannot be in the future");
        }
    }
}
