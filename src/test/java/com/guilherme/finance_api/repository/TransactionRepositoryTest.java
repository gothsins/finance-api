package com.guilherme.finance_api.repository;

import com.guilherme.finance_api.entity.Category;
import com.guilherme.finance_api.entity.Transaction;
import com.guilherme.finance_api.entity.TransactionType;
import com.guilherme.finance_api.entity.User;
import com.guilherme.finance_api.repository.TransactionRepository;
import com.guilherme.finance_api.specification.TransactionSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User userA;
    private User userB;
    private Category food;
    private Category transport;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setEmail("usera@test.com");
        userA.setPassword("123456");
        entityManager.persist(userA);

        userB = new User();
        userB.setEmail("userb@test.com");
        userB.setPassword("123456");
        entityManager.persist(userB);

        food = new Category();
        food.setName("Food");
        entityManager.persist(food);

        transport = new Category();
        transport.setName("Transport");
        entityManager.persist(transport);

        saveTransaction(userA, food, TransactionType.EXPENSE, LocalDate.of(2026, 1, 10), new BigDecimal("100.00"), "Lunch");
        saveTransaction(userA, transport, TransactionType.EXPENSE, LocalDate.of(2026, 2, 15), new BigDecimal("50.00"), "Bus fare");
        saveTransaction(userA, food, TransactionType.INCOME, LocalDate.of(2026, 3, 1), new BigDecimal("2000.00"), "Salary");
        saveTransaction(userB, food, TransactionType.EXPENSE, LocalDate.of(2026, 1, 20), new BigDecimal("80.00"), "Groceries");

        entityManager.flush();
    }

    private void saveTransaction(User user, Category category, TransactionType type, LocalDate date, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setType(type);
        transaction.setDate(date);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        entityManager.persist(transaction);
    }

    @Test
    void hasUser_returnsOnlyThatUsersTransactions() {
        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(userA.getId()));

        Page<Transaction> result = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(t -> t.getUser().getId().equals(userA.getId()));
    }

    @Test
    void hasType_filtersOnlyExpenses() {
        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(userA.getId()))
                .and(TransactionSpecification.hasType(TransactionType.EXPENSE));

        Page<Transaction> result = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(t -> t.getType() == TransactionType.EXPENSE);
    }

    @Test
    void hasCategory_filtersCorrectly() {
        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(userA.getId()))
                .and(TransactionSpecification.hasCategory(transport.getId()));

        Page<Transaction> result = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Bus fare");
    }

    @Test
    void dateBetween_filtersByRange() {
        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(userA.getId()))
                .and(TransactionSpecification.dateBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28)));

        Page<Transaction> result = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void amountBetween_filtersByRange() {
        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(userA.getId()))
                .and(TransactionSpecification.amountBetween(new BigDecimal("60"), new BigDecimal("150")));

        Page<Transaction> result = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Lunch");
    }

    @Test
    void descriptionContains_isCaseInsensitive() {
        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(userA.getId()))
                .and(TransactionSpecification.descriptionContains("LUNCH"));

        Page<Transaction> result = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void combinedFilters_matchesRealFindAllScenario() {
        Specification<Transaction> spec = Specification.<Transaction>unrestricted()
                .and(TransactionSpecification.hasUser(userA.getId()))
                .and(TransactionSpecification.hasType(TransactionType.EXPENSE))
                .and(TransactionSpecification.dateBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));

        Page<Transaction> result = transactionRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Lunch");
    }
}