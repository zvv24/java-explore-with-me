package ru.practicum.rating.service;

import ru.practicum.rating.dto.EventRatingDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingEventFullDto;
import ru.practicum.rating.dto.UserRatingDto;

import java.util.List;

public interface RatingService {
    RatingDto addRating(Long userId, Long eventId, Boolean isLike);

    RatingDto updateRating(Long userId, Long eventId, Boolean isLike);

    EventRatingDto getEventRating(Long eventId);

    UserRatingDto getUserRating(Long userId);

    void removeRating(Long userId, Long eventId);

    List<RatingEventFullDto> getEventsSortedByRating(Integer from, Integer size);

    List<RatingEventFullDto> getEventsSortedByLikes(Integer from, Integer size);

    List<RatingEventFullDto> getEventsSortedByDislikes(Integer from, Integer size);
}
