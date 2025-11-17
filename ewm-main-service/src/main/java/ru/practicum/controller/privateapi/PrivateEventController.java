package ru.practicum.controller.privateapi;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateEventController {
    EventService eventService;
    private static final String PATH_EVENT_ID = "/{eventId}";

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Long userId,
                                                     @Valid @RequestBody NewEventDto newEventDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(userId, newEventDto));
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(eventService.getUserEvents(userId, from, size));
    }

    @GetMapping(PATH_EVENT_ID)
    public ResponseEntity<EventFullDto> getUserEvent(@PathVariable Long userId,
                                                       @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getUserEvent(userId, eventId));
    }

    @PatchMapping(PATH_EVENT_ID)
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                     @PathVariable Long eventId,
                                                     @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return ResponseEntity.ok(eventService.updateEventByUser(userId, eventId, updateRequest));
    }
}