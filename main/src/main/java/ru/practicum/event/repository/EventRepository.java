package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "ORDER BY " +
            "((SELECT COUNT(r) FROM Rating r WHERE r.event = e AND r.isLike = true) - " +
            "(SELECT COUNT(r) FROM Rating r WHERE r.event = e AND r.isLike = false)) DESC")
    List<Event> findAllPublishedWithRatingsSortedByRating(Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "ORDER BY " +
            "(SELECT COUNT(r) FROM Rating r WHERE r.event = e AND r.isLike = true) DESC")
    List<Event> findAllPublishedWithRatingsSortedByLikes(Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "ORDER BY " +
            "(SELECT COUNT(r) FROM Rating r WHERE r.event = e AND r.isLike = false) DESC")
    List<Event> findAllPublishedWithRatingsSortedByDislikes(Pageable pageable);
}
