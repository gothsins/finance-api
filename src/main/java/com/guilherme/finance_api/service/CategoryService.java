package com.guilherme.finance_api.service;

import com.guilherme.finance_api.dto.CategoryRequest;
import com.guilherme.finance_api.entity.Category;
import com.guilherme.finance_api.entity.User;
import com.guilherme.finance_api.exception.ResourceNotFoundException;
import com.guilherme.finance_api.repository.CategoryRepository;
import com.guilherme.finance_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        // Recupera o usuário atual a partir do token JWT.
        // Todas as operações de categoria abaixo usam este usuário para impor isolamento.
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<Category> findAll() {
        User user = getAuthenticatedUser();
        return categoryRepository.findAllByUserId(user.getId());
    }

    public Category findById(Long id) {
        User user = getAuthenticatedUser();
        // Busca a categoria apenas se ela pertencer ao usuário autenticado.
        return categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public Category update(Long id, CategoryRequest request) {
        User user = getAuthenticatedUser();
        // Atualiza somente categorias do usuário autenticado.
        Category categoryExist = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryExist.setName(request.getName());
        return categoryRepository.save(categoryExist);
    }

    public Category save(CategoryRequest request) {
        User user = getAuthenticatedUser();
        // Garante que a nova categoria seja salva já vinculada ao usuário atual.
        Category category = new Category();
        category.setName(request.getName());
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        User user = getAuthenticatedUser();
        if (!categoryRepository.existsByIdAndUserId(id, user.getId())) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
