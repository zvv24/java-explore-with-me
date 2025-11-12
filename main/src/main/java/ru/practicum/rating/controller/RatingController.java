package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.UserRatingDto;
import ru.practicum.rating.service.RatingService;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/events/{eventId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto addLike(@PathVariable Long userId,
                             @PathVariable Long eventId) {
        return ratingService.addRating(userId, eventId, true);
    }

    @PostMapping("/events/{eventId}/dislike")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto addDislike(@PathVariable Long userId,
                                @PathVariable Long eventId) {
        return ratingService.addRating(userId, eventId, false);
    }

    @PutMapping("/events/{eventId}/like")
    public RatingDto updateToLike(@PathVariable Long userId,
                                  @PathVariable Long eventId) {
        return ratingService.updateRating(userId, eventId, true);
    }

    @PutMapping("/events/{eventId}/dislike")
    public RatingDto updateToDislike(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        return ratingService.updateRating(userId, eventId, false);
    }

    @GetMapping("/rating")
    public UserRatingDto getUserRating(@PathVariable Long userId) {
        return ratingService.getUserRating(userId);
    }

    @DeleteMapping("/events/{eventId}/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRating(@PathVariable Long userId,
                             @PathVariable Long eventId) {
        ratingService.removeRating(userId, eventId);
    }
}
