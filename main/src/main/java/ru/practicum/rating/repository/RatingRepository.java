package ru.practicum.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.rating.model.Rating;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    Optional<Rating> findByEventIdAndUserId(Long eventId, Long userId);

    Long countByEventIdAndIsLikeTrue(Long eventId);

    Long countByEventIdAndIsLikeFalse(Long eventId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.event.initiator.id = :userId AND r.isLike = true")
    Long countUserLikes(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.event.initiator.id = :userId AND r.isLike = false")
    Long countUserDislikes(@Param("userId") Long userId);
}
