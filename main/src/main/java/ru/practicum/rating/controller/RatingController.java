package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.rating.dto.EventRatingDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingEventFullDto;
import ru.practicum.rating.dto.UserRatingDto;
import ru.practicum.rating.service.RatingService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/users/{userId}/events/{eventId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto addLike(@PathVariable Long userId,
                             @PathVariable Long eventId) {
        return ratingService.addRating(userId, eventId, true);
    }

    @PostMapping("/users/{userId}/events/{eventId}/dislike")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto addDislike(@PathVariable Long userId,
                                @PathVariable Long eventId) {
        return ratingService.addRating(userId, eventId, false);
    }

    @PutMapping("/users/{userId}/events/{eventId}/like")
    public RatingDto updateToLike(@PathVariable Long userId,
                                  @PathVariable Long eventId) {
        return ratingService.updateRating(userId, eventId, true);
    }

    @PutMapping("/users/{userId}/events/{eventId}/dislike")
    public RatingDto updateToDislike(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        return ratingService.updateRating(userId, eventId, false);
    }

    @GetMapping("/events/{eventId}/rating")
    public EventRatingDto getEventRating(@PathVariable Long eventId) {
        return ratingService.getEventRating(eventId);
    }

    @GetMapping("/users/{userId}/rating")
    public UserRatingDto getUserRating(@PathVariable Long userId) {
        return ratingService.getUserRating(userId);
    }

    @DeleteMapping("/users/{userId}/events/{eventId}/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRating(@PathVariable Long userId,
                             @PathVariable Long eventId) {
        ratingService.removeRating(userId, eventId);
    }

    @GetMapping("/events/rating/sorted")
    public List<RatingEventFullDto> getEventsSorted(
            @RequestParam(defaultValue = "RATING") String sortBy,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        return switch (sortBy.toUpperCase()) {
            case "LIKES" -> ratingService.getEventsSortedByLikes(from, size);
            case "DISLIKES" -> ratingService.getEventsSortedByDislikes(from, size);
            default -> ratingService.getEventsSortedByRating(from, size);
        };
    }
}
