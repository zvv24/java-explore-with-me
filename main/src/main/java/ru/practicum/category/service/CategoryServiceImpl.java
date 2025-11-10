package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.toEntity(categoryDto);
        Category newCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(newCategory);
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id" + categoryId + " не найдена"));

        category.setName(categoryDto.getName());
        Category newCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(newCategory);
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id" + categoryId + " не найдена"));

        return CategoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Page<Category> categories = categoryRepository.findAll(PageRequest.of(from, size));

        return categories.getContent()
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (categoryRepository.existsEventsByCategoryId(categoryId)) {
            throw new ConflictException("В категории присутствуют события");
        }

        categoryRepository.deleteById(categoryId);
    }
}
