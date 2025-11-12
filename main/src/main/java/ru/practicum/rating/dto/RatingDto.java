package ru.practicum.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RatingDto {
    private Long id;
    private Long event;
    private Long user;
    private Boolean isLike;
}
