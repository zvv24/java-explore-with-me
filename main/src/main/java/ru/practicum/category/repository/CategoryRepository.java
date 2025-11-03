package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Boolean existsByName(String name);

    @Query("SELECT COUNT(e) > 0 FROM Event AS e WHERE e.category.id = :categoryId")
    Boolean existsEventsByCategoryId(@Param("categoryId") Long categoryId);
}
