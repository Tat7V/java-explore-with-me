package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.RequestStatus;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestService {
    RequestRepository requestRepository;
    EventRepository eventRepository;
    UserRepository userRepository;

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("Cannot create request for own event");
        }
        if (event.getState() != ru.practicum.model.EventState.PUBLISHED) {
            throw new RuntimeException("Event not published");
        }
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new RuntimeException("Request already exists");
        }
        Long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new RuntimeException("Participant limit reached");
        }
        Request request = new Request();
        request.setEvent(event);
        request.setRequester(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(Boolean.TRUE.equals(event.getRequestModeration()) ? RequestStatus.PENDING : RequestStatus.CONFIRMED);
        }
        request.setCreated(LocalDateTime.now());
        Request saved = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Request does not belong to user");
        }
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new RuntimeException("Cannot cancel confirmed request");
        }
        request.setStatus(RequestStatus.CANCELED);
        Request updated = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(updated);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("User is not the event initiator");
        }
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("User is not the event initiator");
        }
        List<Request> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());
        if (requests.stream().anyMatch(r -> r.getStatus() == RequestStatus.CONFIRMED)) {
            throw new RuntimeException("Cannot cancel confirmed request");
        }
        if (requests.stream().anyMatch(r -> r.getStatus() != RequestStatus.PENDING)) {
            throw new RuntimeException("Request status must be PENDING");
        }
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(new java.util.ArrayList<>());
        result.setRejectedRequests(new java.util.ArrayList<>());
        Long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        for (Request r : requests) {
            if ("CONFIRMED".equals(updateRequest.getStatus())) {
                if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
                    throw new RuntimeException("Participant limit reached");
                }
                r.setStatus(RequestStatus.CONFIRMED);
                confirmed++;
                result.getConfirmedRequests().add(RequestMapper.toParticipationRequestDto(r));
            } else {
                r.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(RequestMapper.toParticipationRequestDto(r));
            }
            requestRepository.save(r);
        }
        return result;
    }
}