package com.guilherme.finance_api.repository;

import com.guilherme.finance_api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
