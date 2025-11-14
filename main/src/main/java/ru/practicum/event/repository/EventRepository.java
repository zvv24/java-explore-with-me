package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT e, " +
            "COALESCE(SUM(CASE WHEN r.isLike = true THEN 1 ELSE 0 END), 0) as likes, " +
            "COALESCE(SUM(CASE WHEN r.isLike = false THEN 1 ELSE 0 END), 0) as dislikes " +
            "FROM Event e " +
            "LEFT JOIN Rating r ON e.id = r.event.id " +
            "WHERE e.state = 'PUBLISHED' " +
            "GROUP BY e " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'LIKES' THEN COALESCE(SUM(CASE WHEN r.isLike = true THEN 1 ELSE 0 END), 0) END DESC, " +
            "CASE WHEN :sortBy = 'DISLIKES' THEN COALESCE(SUM(CASE WHEN r.isLike = false THEN 1 ELSE 0 END), 0) END DESC, " +
            "COALESCE(SUM(CASE WHEN r.isLike = true THEN 1 ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN r.isLike = false THEN 1 ELSE 0 END), 0) DESC")
    List<Object[]> findEventsWithRatings(@Param("sortBy") String sortBy, Pageable pageable);
}
