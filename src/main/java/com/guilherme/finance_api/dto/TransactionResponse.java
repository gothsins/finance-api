package com.guilherme.finance_api.dto;

import com.guilherme.finance_api.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private TransactionType type;
    private String userEmail;
    private String categoryName;
}