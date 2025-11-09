package ru.practicum.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("SELECT DISTINCT c FROM Compilation c " +
            "LEFT JOIN FETCH c.events " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Compilation> findAllWithEvents(@Param("pinned") Boolean pinned, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Compilation c " +
            "LEFT JOIN FETCH c.events " +
            "WHERE c.id = :id")
    Optional<Compilation> findByIdWithEvents(@Param("id") Long id);
}
