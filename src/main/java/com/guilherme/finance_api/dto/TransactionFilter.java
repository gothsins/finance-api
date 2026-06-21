package com.guilherme.finance_api.dto;

import com.guilherme.finance_api.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionFilter(
        Long categoryId,
        TransactionType type,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal minValue,
        BigDecimal maxValue,
        String description
) {}