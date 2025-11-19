package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EventService {
    EventRepository eventRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    RequestRepository requestRepository;
    StatsService statsService;

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format("User with id '%d' not found", userId)));
        var category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new RuntimeException(String.format("Category '%d' not found", newEventDto.getCategory())));

        Event event = EventMapper.toEvent(newEventDto, category, user);
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(savedEvent, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(e,
                        requestRepository.countByEventIdAndStatus(e.getId(), ru.practicum.model.RequestStatus.CONFIRMED),
                        getViews(e.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with id '%d' not found", eventId)));
        return EventMapper.toEventFullDto(event,
                requestRepository.countByEventIdAndStatus(eventId, ru.practicum.model.RequestStatus.CONFIRMED),
                getViews(eventId));
    }

    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with id '%d' not found", eventId)));

        if (event.getState() == EventState.PUBLISHED) {
            throw new RuntimeException("Event already exists");
        }

        updateEventFields(event, updateRequest);

        if (updateRequest.getStateAction() != null) {
            if ("SEND_TO_REVIEW".equals(updateRequest.getStateAction())) {
                event.setState(EventState.PENDING);
            } else if ("CANCEL_REVIEW".equals(updateRequest.getStateAction())) {
                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(updatedEvent,
                requestRepository.countByEventIdAndStatus(eventId, ru.practicum.model.RequestStatus.CONFIRMED),
                getViews(eventId));
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Integer from, Integer size) {
        List<EventState> eventStates =
                (states != null ? states.stream().map(EventState::valueOf).collect(Collectors.toList()) : List.of(EventState.values()));

        log.debug("getEventsByAdmin: users={}, categories={}, states={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, categories, states, rangeStart, rangeEnd, from, size);

        boolean usersAll = (users == null || users.isEmpty());
        boolean categoriesAll = (categories == null || categories.isEmpty());
        List<Long> usersParam = usersAll ? List.of(-1L) : users;
        List<Long> categoriesParam = categoriesAll ? List.of(-1L) : categories;

        List<Event> filtered = eventRepository
                .findAllForAdminFiltered(
                        usersParam,
                        usersAll,
                        eventStates,
                        categoriesParam,
                        categoriesAll,
                        rangeStart,
                        rangeEnd
                );

        log.debug("After DB filtering: {} events", filtered.size());

        int start = Math.min(from, filtered.size());
        int end = Math.min(start + size, filtered.size());
        List<Event> paginated = filtered.subList(start, end);

        log.debug("After pagination: {} events", paginated.size());

        return paginated.stream()
                .map(e -> EventMapper.toEventFullDto(e,
                        requestRepository.countByEventIdAndStatus(e.getId(), ru.practicum.model.RequestStatus.CONFIRMED),
                        getViews(e.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with id '%d' not found", eventId)));

        updateEventFields(event, updateRequest);

        if (updateRequest.getStateAction() != null) {
            if ("PUBLISH_EVENT".equals(updateRequest.getStateAction())) {
                if (event.getState() != EventState.PENDING) {
                    throw new RuntimeException("Cannot publish the event because it's not in the right state");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if ("REJECT_EVENT".equals(updateRequest.getStateAction())) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new RuntimeException("Cannot reject the event because it's already published");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        return EventMapper.toEventFullDto(updated,
                requestRepository.countByEventIdAndStatus(eventId, ru.practicum.model.RequestStatus.CONFIRMED),
                getViews(eventId));
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Boolean onlyAvailable, String sort, Integer from, Integer size,
                                                String uri, String ip) {
        log.debug("getPublicEvents called with text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        final LocalDateTime actualStart = (rangeStart == null && rangeEnd == null) ? LocalDateTime.now() : rangeStart;
        final LocalDateTime actualEnd = rangeEnd;

        if (actualStart != null && actualEnd != null && actualStart.isAfter(actualEnd)) {
            throw new RuntimeException("rangeStart must be before rangeEnd");
        }

        try {
            if (size == null || size <= 0) {
                size = 10;
            }
            if (from == null || from < 0) {
                from = 0;
            }
            Pageable pageable = PageRequest.of(0, 10000);
            Page<Event> allEvents = eventRepository.findAllPublishedEvents(pageable);

            log.debug("Repository returned {} events", allEvents.getTotalElements());

            List<Event> filtered = allEvents.stream()
                    .filter(e -> text == null || text.isEmpty()
                            || (e.getAnnotation() != null && e.getAnnotation().toLowerCase().contains(text.toLowerCase()))
                            || (e.getDescription() != null && e.getDescription().toLowerCase().contains(text.toLowerCase())))
                    .filter(e -> categories == null || categories.isEmpty() || categories.contains(e.getCategory().getId()))
                    .filter(e -> paid == null || e.getPaid().equals(paid))
                    .filter(e -> {
                        LocalDateTime s = actualStart;
                        return s == null || e.getEventDate().isAfter(s) || e.getEventDate().isEqual(s);
                    })
                    .filter(e -> {
                        LocalDateTime e2 = actualEnd;
                        return e2 == null || e.getEventDate().isBefore(e2) || e.getEventDate().isEqual(e2);
                    })
                    .collect(Collectors.toList());

            if (!"VIEWS".equals(sort)) {
                filtered.sort((a, b) -> a.getEventDate().compareTo(b.getEventDate()));
            }

            int start = from;
            int end = Math.min(start + size, filtered.size());
            List<Event> events = filtered.subList(start, end);

            log.debug("After filtering: {} events", events.size());

            for (Event e : events) {
                try {
                    if (e.getCategory() != null) {
                        e.getCategory().getId();
                        e.getCategory().getName();
                    }
                    if (e.getInitiator() != null) {
                        e.getInitiator().getId();
                        e.getInitiator().getName();
                    }
                } catch (Exception ex) {
                    log.error("Error loading related entities for event {}: {}", e.getId(), ex.getMessage(), ex);
                    throw new RuntimeException(String.format("Error loading event data: %s", ex.getMessage()), ex);
                }
            }

            try {
                statsService.saveHit(uri, ip);
            } catch (Exception e) {
                // ignore
            }

            List<EventShortDto> result;
            if (events.isEmpty()) {
                log.debug("No events found, returning empty list");
                result = List.of();
            } else {
                log.debug("Processing {} events", events.size());
                List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
                List<String> eventUris = ids.stream()
                        .map(id -> "/events/" + id)
                        .collect(Collectors.toList());

                Map<String, Long> viewsMap = java.util.Collections.emptyMap();
                try {
                    if (!eventUris.isEmpty()) {
                        LocalDateTime s = LocalDateTime.now().minusYears(1);
                        LocalDateTime e3 = LocalDateTime.now().plusYears(1);
                        List<ru.practicum.stats.dto.ViewStats> allStats =
                                statsService.getStats(s, e3, eventUris, false);
                        if (allStats != null && !allStats.isEmpty()) {
                            viewsMap = allStats.stream()
                                    .filter(v -> v != null && v.getUri() != null)
                                    .collect(Collectors.toMap(
                                            ru.practicum.stats.dto.ViewStats::getUri,
                                            ru.practicum.stats.dto.ViewStats::getHits,
                                            (a, b) -> a
                                    ));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error getting stats: {}", e.getMessage(), e);
                }

                final Map<String, Long> finalViewsMap = viewsMap;
                log.debug("Mapping {} events to DTOs", events.size());
                result = events.stream()
                        .map(e -> {
                            try {
                                Long v = finalViewsMap.getOrDefault("/events/" + e.getId(), 0L);
                                Long confirmed = requestRepository.countByEventIdAndStatus(e.getId(), ru.practicum.model.RequestStatus.CONFIRMED);
                                return EventMapper.toEventShortDto(e, confirmed, v);
                            } catch (Exception ex) {
                                log.error("Error mapping event {}: {}", e.getId(), ex.getMessage(), ex);
                                throw new RuntimeException(String.format("Error mapping event %d: %s", e.getId(), ex.getMessage()), ex);
                            }
                        })
                        .collect(Collectors.toList());
                log.debug("Successfully mapped {} events", result.size());
            }

            if (!"VIEWS".equals(sort) && !result.isEmpty()) {
                result.sort((a, b) -> Long.compare(b.getViews(), a.getViews()));
            }

            return result;
        } catch (Exception e) {
            log.error("Error in getPublicEvents: {}", e.getMessage(), e);
            throw new RuntimeException(String.format("Error getting public events: %s", e.getMessage()), e);
        }
    }

    @Transactional(readOnly = true)
    public EventFullDto getPublicEvent(Long eventId, String uri, String ip) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with id '%d' not found", eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new RuntimeException(String.format("Event with id '%d' not found", eventId));
        }

        Long viewsBefore = getViews(eventId);

        try {
            statsService.saveHit(uri, ip);
        } catch (Exception e) {
            // ignore
        }

        Long views = viewsBefore + 1;

        return EventMapper.toEventFullDto(event,
                requestRepository.countByEventIdAndStatus(eventId, ru.practicum.model.RequestStatus.CONFIRMED),
                views);
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            var category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new RuntimeException(String.format("Category '%d' not found", updateRequest.getCategory())
                    ));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(new ru.practicum.model.Location(
                    updateRequest.getLocation().getLat(),
                    updateRequest.getLocation().getLon()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            var category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new RuntimeException(String.format("Category '%d' not found", updateRequest.getCategory())
                    ));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    private Long getViews(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now().plusYears(1);
            List<ru.practicum.stats.dto.ViewStats> stats =
                    statsService.getStats(start, end, List.of(String.format("/events/%d", eventId)), false);
            return stats.isEmpty() ? 0L : stats.get(0).getHits();
        } catch (Exception e) {
            return 0L;
        }
    }
}