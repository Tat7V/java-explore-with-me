package ru.practicum.mapper;

import ru.practicum.dto.*;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.Location;

public class EventMapper {
    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setLocation(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setTitle(event.getTitle());
        dto.setState(event.getState().name());
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        return dto;
    }

    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            throw new RuntimeException("Event cannot be null");
        }
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        
        try {
            if (event.getCategory() == null) {
                throw new RuntimeException("Event category cannot be null for event " + event.getId());
            }
            dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        } catch (Exception e) {
            throw new RuntimeException("Error mapping category for event " + event.getId() + ": " + e.getMessage(), e);
        }
        
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        
        try {
            if (event.getInitiator() == null) {
                throw new RuntimeException("Event initiator cannot be null for event " + event.getId());
            }
            dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        } catch (Exception e) {
            throw new RuntimeException("Error mapping initiator for event " + event.getId() + ": " + e.getMessage(), e);
        }
        
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        return dto;
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