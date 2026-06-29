package com.guilherme.finance_api.service;

import com.guilherme.finance_api.dto.TransactionFilter;
import com.guilherme.finance_api.dto.TransactionRequest;
import com.guilherme.finance_api.dto.TransactionResponse;
import com.guilherme.finance_api.dto.TransactionSummary;
import com.guilherme.finance_api.entity.Category;
import com.guilherme.finance_api.entity.Transaction;
import com.guilherme.finance_api.entity.TransactionType;
import com.guilherme.finance_api.entity.User;
import com.guilherme.finance_api.event.TransactionEvent;
import com.guilherme.finance_api.event.TransactionPublisher;
import com.guilherme.finance_api.exception.ResourceNotFoundException;
import com.guilherme.finance_api.repository.CategoryRepository;
import com.guilherme.finance_api.repository.TransactionRepository;
import com.guilherme.finance_api.repository.UserRepository;
import com.guilherme.finance_api.specification.TransactionSpecification;
import org.springframework.cache.annotation.Cacheable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Data
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionPublisher transactionPublisher;

    public Page<TransactionResponse> findAll(TransactionFilter filter, Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(user.getId()))
                .and(TransactionSpecification.hasCategory(filter.categoryId()))
                .and(TransactionSpecification.hasType(filter.type()))
                .and(TransactionSpecification.dateBetween(filter.startDate(), filter.endDate()))
                .and(TransactionSpecification.amountBetween(filter.minValue(), filter.maxValue()))
                .and(TransactionSpecification.descriptionContains(filter.description()));

        return transactionRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    public TransactionResponse findById(Long id) {
        User user = getAuthenticatedUser();
        // Busca a transação somente se ela pertencer ao usuário autenticado.
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getUser().getEmail(),
                transaction.getCategory() != null ? transaction.getCategory().getName() : null
        );
    }

    public TransactionResponse update(Long id, TransactionRequest request) {
        User user = getAuthenticatedUser();
        // Garante que só o dono possa alterar a transação.
        Transaction transactionExist = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionExist.setDescription(request.getDescription());
        transactionExist.setAmount(request.getAmount());
        transactionExist.setDate(request.getDate());
        transactionExist.setType(request.getType());
        transactionExist.setCategory(categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
        Transaction savedTransaction = transactionRepository.save(transactionExist);
        return toResponse(savedTransaction);
    }

    @CacheEvict(value = "transaction-summary", allEntries = true)
    public TransactionResponse save(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setType(request.getType());

        User user = getAuthenticatedUser();

        // Valida se a categoria pertence ao usuário antes de salvar a transação.
        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        transaction.setUser(user);
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionEvent event = new TransactionEvent(
                savedTransaction.getId(),
                savedTransaction.getDescription(),
                savedTransaction.getAmount(),
                savedTransaction.getDate(),
                savedTransaction.getType().name(),
                savedTransaction.getUser().getEmail(),
                savedTransaction.getCategory() != null ? savedTransaction.getCategory().getName() : null
        );
        transactionPublisher.publish(event);
        return toResponse(savedTransaction);
    }

    @Cacheable(value = "transaction-summary", key = "#month ?: 'current' + '::' + #email")
    public TransactionSummary getSummary(String month, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        YearMonth yearMonth = month != null
                ? YearMonth.parse(month)
                : YearMonth.now();

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(user.getId()))
                .and(TransactionSpecification.dateBetween(start, end));

        List<Transaction> transactions = transactionRepository.findAll(spec);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TransactionSummary(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        );
    }

    public void delete(Long id) {
        User user = getAuthenticatedUser();
        // Só permite excluir transações do usuário autenticado.
        if (!transactionRepository.existsByIdAndUserId(id, user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        transactionRepository.deleteById(id);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
