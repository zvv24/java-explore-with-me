package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StateService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class StateController {
    private final StateService stateService;
    private static final String timeFormat = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @Validated
    public EndpointHitDto hit(@RequestBody EndpointHitDto endpointHitDto) {
        return stateService.hit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> stats(
            @RequestParam @DateTimeFormat(pattern = timeFormat) LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = timeFormat) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        return stateService.stats(start, end, uris, unique);
    }
}
