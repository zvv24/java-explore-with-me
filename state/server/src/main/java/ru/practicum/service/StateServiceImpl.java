package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StateMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StateServerRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StateServiceImpl implements StateService {
    private final StateServerRepository stateServerRepository;
    private final StateMapper stateMapper;

    @Override
    public EndpointHitDto hit(EndpointHitDto endpointHitDto) {
        EndpointHit entity = stateMapper.toEntity(endpointHitDto);
        EndpointHit saved = stateServerRepository.save(entity);
        return stateMapper.toDto(saved);
    }

    @Override
    public List<ViewStats> stats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        if (unique) {
            if (uris != null && !uris.isEmpty()) {
                return stateServerRepository.findUniqueStatsWithUris(start, end, uris);
            } else {
                return stateServerRepository.findUniqueStatsWithoutUris(start, end);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                return stateServerRepository.findStatsWithUris(start, end, uris);
            } else {
                return stateServerRepository.findStatsWithoutUris(start, end);
            }
        }
    }
}
