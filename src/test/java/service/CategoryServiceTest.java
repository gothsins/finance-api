package service;

import com.guilherme.finance_api.dto.CategoryRequest;
import com.guilherme.finance_api.entity.Category;
import com.guilherme.finance_api.entity.User;
import com.guilherme.finance_api.exception.ResourceNotFoundException;
import com.guilherme.finance_api.repository.CategoryRepository;
import com.guilherme.finance_api.repository.UserRepository;
import com.guilherme.finance_api.service.CategoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null)
        );
    }

    private User authenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        return user;
    }

    @Test
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        authenticate("test@example.com");
        User user = authenticatedUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
    }

    @Test
    void save_ShouldReturnSavedCategory() {
        // Arrange
        authenticate("test@example.com");
        User user = authenticatedUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        CategoryRequest request = new CategoryRequest();
        request.setName("Food");

        Category category = new Category();
        category.setName("Food");
        category.setUser(user);

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category result = categoryService.save(request);

        // Assert
        assertEquals("Food", result.getName());
    }

    @Test
    void delete_ShouldThrowException_WhenIdDoesNotExist() {
        authenticate("test@example.com");
        User user = authenticatedUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.existsByIdAndUserId(99L, 1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(99L));
    }

    @Test
    void findById_ShouldReturnCategory_WhenIdExists() {
        // Arrange
        authenticate("test@example.com");
        User user = authenticatedUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Category category = new Category();
        category.setId(1L);
        category.setName("Food");
        category.setUser(user);

        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.findById(1L);

        // Assert
        assertEquals("Food", result.getName());
    }

    @Test
    void delete_ShouldDeleteCategory_WhenIdExists() {
        authenticate("test@example.com");
        User user = authenticatedUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        authenticate("test@example.com");
        User user = authenticatedUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Category category1 = new Category();
        category1.setName("Food");
        category1.setUser(user);

        Category category2 = new Category();
        category2.setName("Transport");
        category2.setUser(user);

        when(categoryRepository.findAllByUserId(1L)).thenReturn(List.of(category1, category2));

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
    }


}