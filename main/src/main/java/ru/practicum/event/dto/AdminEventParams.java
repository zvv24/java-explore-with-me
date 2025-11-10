package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminEventParams {
    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private Integer from = 0;
    private Integer size = 10;
}
