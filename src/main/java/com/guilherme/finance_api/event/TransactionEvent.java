package com.guilherme.finance_api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String type;
    private String userEmail;
    private String categoryName;
}