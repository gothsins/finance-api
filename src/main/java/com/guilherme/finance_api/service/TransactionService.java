package com.guilherme.finance_api.service;

import com.guilherme.finance_api.entity.Transaction;
import com.guilherme.finance_api.entity.User;
import com.guilherme.finance_api.exception.ResourceNotFoundException;
import com.guilherme.finance_api.repository.TransactionRepository;
import com.guilherme.finance_api.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    public Transaction save(Transaction transaction) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        transaction.setUser(user);
        return transactionRepository.save(transaction);
    }

    public void delete(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        transactionRepository.deleteById(id);
    }
}
