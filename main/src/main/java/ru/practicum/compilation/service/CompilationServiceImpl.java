package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        } else {
            compilation.setEvents(new HashSet<>());
        }

        try {
            Compilation savedCompilation = compilationRepository.save(compilation);
            return compilationMapper.toDto(savedCompilation);
        } catch (Exception e) {
            throw new ValidationException("Подборка с таким названием уже существует");
        }
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findByIdWithEvents(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        compilationMapper.updateCompilationFromRequest(updateRequest, compilation);

        if (updateRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateRequest.getEvents());
            compilation.setEvents(new HashSet<>(events));
        }

        try {
            Compilation updatedCompilation = compilationRepository.save(compilation);
            return compilationMapper.toDto(updatedCompilation);
        } catch (Exception e) {
            throw new ValidationException("Подборка с таким названием уже существует");
        }
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findByIdWithEvents(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));

        return compilationMapper.toDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findAllWithEvents(pinned, pageable);
        } else {
            compilations = compilationRepository.findAllWithEvents(null, pageable);
        }

        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }
}
