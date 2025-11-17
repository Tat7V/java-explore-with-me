package ru.practicum.mapper;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.RequestStatus;
import ru.practicum.repository.RequestRepository;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {
    public static CompilationDto toCompilationDto(Compilation compilation,
                                                   RequestRepository requestRepository,
                                                   StatsService statsService) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());

        List<EventShortDto> events = compilation.getEvents().stream()
                .map(e -> {
                    Long confirmedRequests = requestRepository.countByEventIdAndStatus(
                            e.getId(), RequestStatus.CONFIRMED);
                    Long views = getViews(e.getId(), statsService);
                    return EventMapper.toEventShortDto(e, confirmedRequests, views);
                })
                .collect(Collectors.toList());

        dto.setEvents(events);
        return dto;
    }

    private static Long getViews(Long eventId, StatsService statsService) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now().plusYears(1);
            List<ru.practicum.stats.dto.ViewStats> stats = statsService.getStats(
                    start, end, List.of("/events/" + eventId), false);
            return stats.isEmpty() ? 0L : stats.get(0).getHits();
        } catch (Exception e) {
            return 0L;
        }
    }
}