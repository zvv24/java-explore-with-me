package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StateClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.rating.dto.EventRatingDto;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StateClient stateClient;
    private final RatingRepository ratingRepository;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id " + newEventDto.getCategory() + " не найдена"));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть не раньше чем через 2 часа от текущего момента");
        }

        Event event = eventMapper.toEntity(newEventDto, user, category);

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя редактировать опубликованное событие");
        }

        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть не раньше чем через 2 часа от текущего момента");
        }

        if (updateRequest.getParticipantLimit() != null && updateRequest.getParticipantLimit() < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + updateRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        eventMapper.updateEventFromUserRequest(updateRequest, event);

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Дата начала события должна быть не ранее чем за час от даты публикации");
        }

        if (updateRequest.getParticipantLimit() != null && updateRequest.getParticipantLimit() < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + updateRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        eventMapper.updateEventFromAdminRequest(updateRequest, event);

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Событие можно публиковать только если оно в состоянии ожидания публикации");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Событие можно отклонить только если оно еще не опубликовано");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        Long views = getEventViews(eventId);
        event.setViews(views);

        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие не опубликовано");
        }

        Long views = getEventViews(eventId);
        event.setViews(views);

        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable)
                .getContent();

        Map<Long, Long> viewsMap = getEventsViews(events);
        Map<Long, EventRatingDto> ratingsMap = getEventsRatings(events);

        List<EventShortDto> result = new ArrayList<>();
        for (Event event : events) {
            EventShortDto shortDto = eventMapper.toShortDto(event);
            shortDto.setViews(viewsMap.getOrDefault(event.getId(), 0L));

            EventRatingDto rating = ratingsMap.get(event.getId());
            if (rating != null) {
                shortDto.setLikes(rating.getLikes());
                shortDto.setDislikes(rating.getDislikes());
                shortDto.setRating(rating.getRating());
            } else {
                shortDto.setLikes(0L);
                shortDto.setDislikes(0L);
                shortDto.setRating(0L);
            }

            result.add(shortDto);
        }

        return result;
    }

    @Override
    public List<EventFullDto> getAdminEvents(AdminEventParams params) {
        try {
            if (params.getRangeStart() != null && params.getRangeEnd() != null &&
                    params.getRangeStart().isAfter(params.getRangeEnd())) {
                throw new ValidationException("Дата начала не может быть позже даты окончания");
            }

            List<Event> allEvents = eventRepository.findAll();

            List<Event> filteredEvents = allEvents.stream()
                    .filter(event -> params.getUsers() == null || params.getUsers().isEmpty() ||
                            params.getUsers().contains(event.getInitiator().getId()))
                    .filter(event -> params.getStates() == null || params.getStates().isEmpty() ||
                            params.getStates().contains(event.getState()))
                    .filter(event -> params.getCategories() == null || params.getCategories().isEmpty() ||
                            params.getCategories().contains(event.getCategory().getId()))
                    .filter(event -> params.getRangeStart() == null ||
                            !event.getEventDate().isBefore(params.getRangeStart()))
                    .filter(event -> params.getRangeEnd() == null ||
                            !event.getEventDate().isAfter(params.getRangeEnd()))
                    .toList();

            int startIndex = Math.min(params.getFrom(), filteredEvents.size());
            int endIndex = Math.min(params.getFrom() + params.getSize(), filteredEvents.size());
            List<Event> paginatedEvents = filteredEvents.subList(startIndex, endIndex);

            Map<Long, Long> viewsMap = getEventsViews(paginatedEvents);
            Map<Long, EventRatingDto> ratingsMap = getEventsRatings(paginatedEvents);

            List<EventFullDto> result = new ArrayList<>();
            for (Event event : paginatedEvents) {
                EventFullDto fullDto = eventMapper.toFullDto(event);
                fullDto.setViews(viewsMap.getOrDefault(event.getId(), 0L));

                EventRatingDto rating = ratingsMap.get(event.getId());
                if (rating != null) {
                    fullDto.setLikes(rating.getLikes());
                    fullDto.setDislikes(rating.getDislikes());
                    fullDto.setRating(rating.getRating());
                } else {
                    fullDto.setLikes(0L);
                    fullDto.setDislikes(0L);
                    fullDto.setRating(0L);
                }

                result.add(fullDto);
            }

            return result;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<EventShortDto> getPublicEvents(PublicEventParams params) {
        try {
            if (params.getRangeStart() != null && params.getRangeEnd() != null &&
                    params.getRangeStart().isAfter(params.getRangeEnd())) {
                throw new ValidationException("Дата начала не может быть позже даты окончания");
            }

            List<Event> allEvents = eventRepository.findAll();

            List<Event> filteredEvents = allEvents.stream()
                    .filter(event -> event.getState() == EventState.PUBLISHED)
                    .filter(event -> params.getText() == null || params.getText().isEmpty() ||
                            event.getAnnotation().toLowerCase().contains(params.getText().toLowerCase()) ||
                            event.getDescription().toLowerCase().contains(params.getText().toLowerCase()))
                    .filter(event -> params.getCategories() == null || params.getCategories().isEmpty() ||
                            params.getCategories().contains(event.getCategory().getId()))
                    .filter(event -> params.getPaid() == null || event.getPaid() == params.getPaid())
                    .filter(event -> params.getRangeStart() == null ||
                            !event.getEventDate().isBefore(params.getRangeStart()))
                    .filter(event -> params.getRangeEnd() == null ||
                            !event.getEventDate().isAfter(params.getRangeEnd()))
                    .filter(event -> !Boolean.TRUE.equals(params.getOnlyAvailable()) ||
                            event.getParticipantLimit() == 0 ||
                            (event.getConfirmedRequests() != null && event.getConfirmedRequests() <
                                    event.getParticipantLimit()))
                    .toList();

            int startIndex = Math.min(params.getFrom(), filteredEvents.size());
            int endIndex = Math.min(params.getFrom() + params.getSize(), filteredEvents.size());
            List<Event> paginatedEvents = filteredEvents.subList(startIndex, endIndex);

            Map<Long, Long> viewsMap = getEventsViews(paginatedEvents);
            Map<Long, EventRatingDto> ratingsMap = getEventsRatings(paginatedEvents);

            List<EventShortDto> result = new ArrayList<>();
            for (Event event : paginatedEvents) {
                EventShortDto shortDto = eventMapper.toShortDto(event);
                shortDto.setViews(viewsMap.getOrDefault(event.getId(), 0L));

                EventRatingDto rating = ratingsMap.get(event.getId());
                if (rating != null) {
                    shortDto.setLikes(rating.getLikes());
                    shortDto.setDislikes(rating.getDislikes());
                    shortDto.setRating(rating.getRating());
                } else {
                    shortDto.setLikes(0L);
                    shortDto.setDislikes(0L);
                    shortDto.setRating(0L);
                }

                result.add(shortDto);
            }

            if ("VIEWS".equals(params.getSort())) {
                result.sort((e1, e2) -> Long.compare(
                        e2.getViews() != null ? e2.getViews() : 0L,
                        e1.getViews() != null ? e1.getViews() : 0L
                ));
            } else if ("EVENT_DATE".equals(params.getSort())) {
                result.sort((e1, e2) -> e2.getEventDate().compareTo(e1.getEventDate()));
            }

            return result;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Long getEventViews(Long eventId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

            if (event.getPublishedOn() == null) {
                return 0L;
            }

            LocalDateTime start = event.getPublishedOn();
            LocalDateTime end = LocalDateTime.now();
            List<String> uris = List.of("/events/" + eventId);

            return stateClient.stats(start, end, uris, true)
                    .stream()
                    .findFirst()
                    .map(stats -> stats.getHits() != null ? stats.getHits() : 0L)
                    .orElse(0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        try {
            List<String> uris = events.stream()
                    .map(event -> "/events/" + event.getId())
                    .collect(Collectors.toList());

            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now();

            return stateClient.stats(start, end, uris, true)
                    .stream()
                    .collect(Collectors.toMap(
                            stats -> extractEventIdFromUri(stats.getUri()),
                            stats -> stats.getHits() != null ? stats.getHits() : 0L,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            return events.stream()
                    .collect(Collectors.toMap(Event::getId, event -> 0L));
        }
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            throw new ValidationException("Неверный формат URI: " + uri);
        }
    }

    private Map<Long, EventRatingDto> getEventsRatings(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> likesMap = ratingRepository.findByEventIdIn(eventIds)
                .stream()
                .filter(Rating::getIsLike)
                .collect(Collectors.groupingBy(
                        rating -> rating.getEvent().getId(),
                        Collectors.counting()
                ));

        Map<Long, Long> dislikesMap = ratingRepository.findByEventIdIn(eventIds)
                .stream()
                .filter(rating -> !rating.getIsLike())
                .collect(Collectors.groupingBy(
                        rating -> rating.getEvent().getId(),
                        Collectors.counting()
                ));

        return eventIds.stream()
                .collect(Collectors.toMap(
                        eventId -> eventId,
                        eventId -> {
                            Long likes = likesMap.getOrDefault(eventId, 0L);
                            Long dislikes = dislikesMap.getOrDefault(eventId, 0L);
                            Long rating = likes - dislikes;
                            return new EventRatingDto(eventId, "", likes, dislikes, rating);
                        }
                ));
    }
}