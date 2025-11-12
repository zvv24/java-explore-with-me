package ru.practicum.rating.mapper;

import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.model.Event;
import ru.practicum.rating.dto.EventRatingDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingEventFullDto;
import ru.practicum.rating.model.Rating;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

public class RatingMapper {
    public static RatingDto toDto(Rating rating) {
        return new RatingDto(
                rating.getId(),
                rating.getEvent().getId(),
                rating.getUser().getId(),
                rating.getIsLike()
        );
    }

    public static Rating toEntity(User user, Event event, Boolean isLike) {
        Rating rating = new Rating();
        rating.setUser(user);
        rating.setEvent(event);
        rating.setIsLike(isLike);
        return rating;
    }

    public static RatingEventFullDto toRatingEventDto(Event event, EventRatingDto rating) {
        return new RatingEventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toDto(event.getCategory()),
                event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L,
                event.getEventDate(),
                UserMapper.toShortDto(event.getInitiator()),
                event.getPaid() != null ? event.getPaid() : false,
                event.getTitle(),
                event.getViews() != null ? event.getViews() : 0L,
                rating.getLikes(),
                rating.getDislikes(),
                rating.getRating()
        );
    }
}
