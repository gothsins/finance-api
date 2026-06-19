package service;

import com.guilherme.finance_api.dto.CategoryRequest;
import com.guilherme.finance_api.entity.Category;
import com.guilherme.finance_api.exception.ResourceNotFoundException;
import com.guilherme.finance_api.repository.CategoryRepository;
import com.guilherme.finance_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
    }

    @Test
    void save_ShouldReturnSavedCategory() {
        // Arrange
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");

        Category category = new Category();
        category.setName("Food");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category result = categoryService.save(request);

        // Assert
        assertEquals("Food", result.getName());
    }

    @Test
    void delete_ShouldThrowException_WhenIdDoesNotExist() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(99L));
    }

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
    void delete_ShouldDeleteCategory_WhenIdExists() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        Category category1 = new Category();
        category1.setName("Food");

        Category category2 = new Category();
        category2.setName("Transport");

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
    }


}