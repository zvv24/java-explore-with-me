package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StateMapper {
    public EndpointHit toEntity(EndpointHitDto endpointHitDto) {
        EndpointHit entity = new EndpointHit();
        entity.setId(endpointHitDto.getId());
        entity.setApp(endpointHitDto.getApp());
        entity.setUri(endpointHitDto.getUri());
        entity.setIp(endpointHitDto.getIp());
        entity.setTimestamp(LocalDateTime.parse(endpointHitDto.getTimestamp(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return entity;
    }

    public EndpointHitDto toDto(EndpointHit endpointHit) {
        EndpointHitDto dto = new EndpointHitDto();
        dto.setId(endpointHit.getId());
        dto.setApp(endpointHit.getApp());
        dto.setUri(endpointHit.getUri());
        dto.setIp(endpointHit.getIp());
        dto.setTimestamp(endpointHit.getTimestamp().toString());
        return dto;
    }
}
