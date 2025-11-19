package ru.practicum.mapper;

import ru.practicum.dto.*;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.Location;

public class EventMapper {
    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setLocation(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()));
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setState(event.getState().name());
        eventFullDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setConfirmedRequests(confirmedRequests);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            throw new RuntimeException("Event cannot be null");
        }
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setAnnotation(event.getAnnotation());

        try {
            if (event.getCategory() == null) {
                throw new RuntimeException(String.format("Event category cannot be null for event %d", event.getId()));
            }
            eventShortDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error mapping category for event %d: %s", event.getId(), e.getMessage()), e);
        }

        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setTitle(event.getTitle());

        try {
            if (event.getInitiator() == null) {
                throw new RuntimeException(String.format("Event initiator cannot be null for event %d", event.getId()));
            }
            eventShortDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error mapping initiator for event %d: %s", event.getId(), e.getMessage()), e);
        }

        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setViews(views);
        return eventShortDto;
    }

    public static Event toEvent(NewEventDto newEventDto, ru.practicum.model.Category category, ru.practicum.model.User initiator) {
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setLocation(new Location(newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon()));
        event.setPaid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false);
        event.setParticipantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0);
        event.setRequestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true);
        event.setTitle(newEventDto.getTitle());
        event.setState(EventState.PENDING);
        event.setInitiator(initiator);
        event.setCreatedOn(java.time.LocalDateTime.now());
        return event;
    }
}