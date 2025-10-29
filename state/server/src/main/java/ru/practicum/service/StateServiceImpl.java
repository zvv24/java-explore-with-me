package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
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
        if (unique) {
            return stateServerRepository.findUniqueStats(start, end, uris);
        } else {
            return stateServerRepository.findStats(start, end, uris);
        }
    }
}
