package com.guilherme.finance_api.service;

import com.guilherme.finance_api.dto.TransactionRequest;
import com.guilherme.finance_api.dto.TransactionResponse;
import com.guilherme.finance_api.entity.Category;
import com.guilherme.finance_api.entity.Transaction;
import com.guilherme.finance_api.entity.User;
import com.guilherme.finance_api.exception.ResourceNotFoundException;
import com.guilherme.finance_api.repository.CategoryRepository;
import com.guilherme.finance_api.repository.TransactionRepository;
import com.guilherme.finance_api.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll()
                .stream()
                .map(transaction -> toResponse(transaction))
                .collect(Collectors.toList());
    }

    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
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
        Transaction transactionExist = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionExist.setDescription(request.getDescription());
        transactionExist.setAmount(request.getAmount());
        transactionExist.setDate(request.getDate());
        transactionExist.setType(request.getType());
        transactionExist.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
        Transaction savedTransaction = transactionRepository.save(transactionExist);
        return toResponse(savedTransaction);
    }

    public TransactionResponse save(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setType(request.getType());

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        transaction.setUser(user);
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return toResponse(savedTransaction);
    }

    public void delete(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        transactionRepository.deleteById(id);
    }
}
