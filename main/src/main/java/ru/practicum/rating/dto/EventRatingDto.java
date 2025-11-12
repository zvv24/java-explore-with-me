package ru.practicum.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventRatingDto {
    private Long eventId;
    private String eventName;
    private Long likes;
    private Long dislikes;
    private Long rating;
}
