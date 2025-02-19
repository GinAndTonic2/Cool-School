package com.coolSchool.CoolSchool.serviceTest;

import com.coolSchool.CoolSchool.exceptions.category.CategoryNotFoundException;
import com.coolSchool.CoolSchool.exceptions.category.ValidationCategoryException;
import com.coolSchool.CoolSchool.models.dto.common.CategoryDTO;
import com.coolSchool.CoolSchool.models.entity.Category;
import com.coolSchool.CoolSchool.repositories.CategoryRepository;
import com.coolSchool.CoolSchool.services.impl.CategoryServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private ModelMapper modelMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        categoryService = new CategoryServiceImpl(categoryRepository, modelMapper, validator);
    }

    @Test
    public void testDeleteCategory_CategoryPresent() {
        Long categoryId = 1L;

        Category category = new Category();
        category.setDeleted(false);

        Optional<Category> categoryOptional = Optional.of(category);

        when(categoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(categoryOptional);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        assertDoesNotThrow(() -> categoryService.deleteCategory(categoryId));
        assertTrue(category.isDeleted());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void testGetAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category());
        Mockito.when(categoryRepository.findByDeletedFalse()).thenReturn(categoryList);
        List<CategoryDTO> result = categoryService.getAllCategories();
        assertNotNull(result);
        assertEquals(categoryList.size(), result.size());
    }

    @Test
    void testGetCategoryById() {
        Long categoryId = 1L;
        Category category = new Category();
        Optional<Category> categoryOptional = Optional.of(category);
        when(categoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(categoryOptional);
        CategoryDTO result = categoryService.getCategoryById(categoryId);
        assertNotNull(result);
    }

    @Test
    void testGetCategoryByIdNotFound() {
        Long categoryId = 1L;
        Optional<Category> categoryOptional = Optional.empty();
        when(categoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(categoryOptional);
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(categoryId));
    }

    @Test
    void testCreateCategory() {
        CategoryDTO categoryDTO = new CategoryDTO();
        Category category = modelMapper.map(categoryDTO, Category.class);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryService.createCategory(categoryDTO);
        assertNotNull(result);
    }

    @Test
    void testUpdateCategory() {
        Long categoryId = 1L;
        CategoryDTO updatedCategoryDTO = new CategoryDTO();
        Category existingCategory = new Category();
        Optional<Category> existingCategoryOptional = Optional.of(existingCategory);
        when(categoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(existingCategoryOptional);
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);
        CategoryDTO result = categoryService.updateCategory(categoryId, updatedCategoryDTO);
        assertNotNull(result);
    }

    @Test
    void testUpdateCategoryNotFound() {
        Long nonExistentCategoryId = 99L;
        CategoryDTO updatedCategoryDTO = new CategoryDTO();
        when(categoryRepository.findByIdAndDeletedFalse(nonExistentCategoryId)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.updateCategory(nonExistentCategoryId, updatedCategoryDTO));
    }

    @Test
    void testDeleteCategoryNotFound() {
        Long nonExistentCategoryId = 99L;

        when(categoryRepository.findByIdAndDeletedFalse(nonExistentCategoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory(nonExistentCategoryId));
    }

    @Test
    void testCreateCategory_ValidationException() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName(null);

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);

        ConstraintViolationException constraintViolationException = new ConstraintViolationException("Validation error", violations);

        when(categoryRepository.save(any(Category.class))).thenThrow(constraintViolationException);

        assertThrows(ValidationCategoryException.class, () -> categoryService.createCategory(categoryDTO));
    }

    @Test
    void testUpdateCategory_ValidationException() {
        Long categoryId = 1L;
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName(null);
        Category existingCategory = new Category();
        Optional<Category> existingCategoryOptional = Optional.of(existingCategory);

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);

        ConstraintViolationException constraintViolationException = new ConstraintViolationException("Validation error", violations);

        when(categoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(existingCategoryOptional);
        when(categoryRepository.save(any(Category.class))).thenThrow(constraintViolationException);

        assertThrows(ConstraintViolationException.class, () -> categoryService.updateCategory(categoryId, categoryDTO));
    }
}

