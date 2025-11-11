package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    CategoryDto getCategory(Long categoryId);

    List<CategoryDto> getCategories(Integer from, Integer size);

    void deleteCategory(Long categoryId);
}
