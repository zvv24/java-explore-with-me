package ru.practicum.event.service;

import ru.practicum.event.dto.*;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto getPublicEvent(Long eventId);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    List<EventFullDto> getAdminEvents(AdminEventParams params);

    List<EventShortDto> getPublicEvents(PublicEventParams params);
}
