package service;

import com.guilherme.finance_api.entity.Category;
import com.guilherme.finance_api.exception.ResourceNotFoundException;
import com.guilherme.finance_api.repository.CategoryRepository;
import com.guilherme.finance_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findById_ShouldReturnCategory_WhenIdExists() {
        // Arrange
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.findById(1L);

        // Assert
        assertEquals("Food", result.getName());
    }

    @Test
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
    }

}