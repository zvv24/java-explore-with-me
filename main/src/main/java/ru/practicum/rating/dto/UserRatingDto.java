package ru.practicum.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRatingDto {
    private Long userId;
    private String userName;
    private Long likes;
    private Long dislikes;
    private Long rating;
}
