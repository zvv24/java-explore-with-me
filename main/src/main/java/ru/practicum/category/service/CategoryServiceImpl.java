package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
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
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с таким именем уже существует");
        }

        Category category = categoryMapper.toEntity(newCategoryDto);
        Category newCategory = categoryRepository.save(category);
        return categoryMapper.toDto(newCategory);
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id" + categoryId + " не найдена"));

        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория с таким именем уже существует");
        }

        category.setName(categoryDto.getName());
        Category newCategory = categoryRepository.save(category);
        return categoryMapper.toDto(newCategory);
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id" + categoryId + " не найдена"));

        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Page<Category> categories = categoryRepository.findAll(PageRequest.of(from, size));

        return categories.getContent()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsEventsByCategoryId(categoryId)) {
            throw new ConflictException("В категории присутствуют события");
        }

        categoryRepository.deleteById(categoryId);
    }
}
