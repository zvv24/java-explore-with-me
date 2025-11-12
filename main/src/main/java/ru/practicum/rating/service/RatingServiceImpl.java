package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.rating.dto.EventRatingDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingEventFullDto;
import ru.practicum.rating.dto.UserRatingDto;
import ru.practicum.rating.mapper.RatingMapper;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RatingRepository ratingRepository;

    @Override
    public RatingDto addRating(Long userId, Long eventId, Boolean isLike) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (ratingRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new ConflictException("Пользователь уже оценил это событие");
        }

        Rating rating = RatingMapper.toEntity(user, event, isLike);
        return RatingMapper.toDto(ratingRepository.save(rating));
    }

    @Override
    public RatingDto updateRating(Long userId, Long eventId, Boolean isLike) {
        Rating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Оценка не найдена"));

        rating.setIsLike(isLike);
        return RatingMapper.toDto(ratingRepository.save(rating));
    }

    @Override
    public EventRatingDto getEventRating(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        Long likes = ratingRepository.countByEventIdAndIsLikeTrue(eventId);
        Long dislikes = ratingRepository.countByEventIdAndIsLikeFalse(eventId);
        Long rating = likes - dislikes;

        return new EventRatingDto(eventId, event.getTitle(), likes, dislikes, rating);
    }

    @Override
    public UserRatingDto getUserRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Long likes = ratingRepository.countUserLikes(userId);
        Long dislikes = ratingRepository.countUserDislikes(userId);
        Long rating = likes - dislikes;

        return new UserRatingDto(userId, user.getName(), likes, dislikes, rating);
    }

    @Override
    public void removeRating(Long userId, Long eventId) {
        Rating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Оценка не найдена"));

        ratingRepository.delete(rating);
    }

    @Override
    public List<RatingEventFullDto> getEventsSortedByRating(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(pageable).getContent();

        return events.stream()
                .map(event -> {
                    EventRatingDto rating = getEventRating(event.getId());
                    return RatingMapper.toRatingEventDto(event, rating);
                })
                .sorted((e1, e2) -> Long.compare(e2.getRating(), e1.getRating()))
                .toList();
    }

    @Override
    public List<RatingEventFullDto> getEventsSortedByLikes(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(pageable).getContent();

        return events.stream()
                .map(event -> {
                    EventRatingDto rating = getEventRating(event.getId());
                    return RatingMapper.toRatingEventDto(event, rating);
                })
                .sorted((e1, e2) -> Long.compare(e2.getLikes(), e1.getLikes()))
                .toList();
    }

    @Override
    public List<RatingEventFullDto> getEventsSortedByDislikes(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(pageable).getContent();

        return events.stream()
                .map(event -> {
                    EventRatingDto rating = getEventRating(event.getId());
                    return RatingMapper.toRatingEventDto(event, rating);
                })
                .sorted((e1, e2) -> Long.compare(e2.getDislikes(), e1.getDislikes()))
                .toList();
    }
}
