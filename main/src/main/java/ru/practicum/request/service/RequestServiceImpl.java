package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подавать заявку на участие в своём событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Нельзя добавить повторный запрос");
        }
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит запросов на участие");
        }

        Request request = RequestMapper.toNewEntity(event, user);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        Request savedRequest = requestRepository.save(request);
        return RequestMapper.toDto(savedRequest);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является инициатором события");
        }

        List<Request> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());

        for (Request request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Запрос с id " + request.getId() +
                        " не принадлежит событию с id " + eventId);
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();
        Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);

        for (Request request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
            }

            if (updateRequest.getStatus().equals("CONFIRMED")) {
                if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                    throw new ConflictException("Достигнут лимит участников для события");
                }

                request.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                confirmedRequests.add(RequestMapper.toDto(request));

                event.setConfirmedRequests(confirmedCount);
            } else if (updateRequest.getStatus().equals("REJECTED")) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(RequestMapper.toDto(request));
            }
        }

        eventRepository.save(event);
        requestRepository.saveAll(requests);

        if (updateRequest.getStatus().equals("CONFIRMED") &&
                event.getParticipantLimit() > 0 &&
                confirmedCount >= event.getParticipantLimit()) {

            List<Request> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
            for (Request pendingRequest : pendingRequests) {
                pendingRequest.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(RequestMapper.toDto(pendingRequest));
            }
            requestRepository.saveAll(pendingRequests);
        }

        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        return result;
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является инициатором события");
        }

        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        request.setStatus(RequestStatus.CANCELED);

        Request canceledRequest = requestRepository.save(request);
        return RequestMapper.toDto(canceledRequest);
    }
}
