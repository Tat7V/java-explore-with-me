package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.*;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationService {
    CompilationRepository compilationRepository;
    EventRepository eventRepository;
    RequestRepository requestRepository;
    StatsService statsService;

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findByIdIn(newCompilationDto.getEvents().stream().collect(Collectors.toList())));
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new HashSet<>());
        }
        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(saved, requestRepository, statsService);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new RuntimeException(String.format("Compilation with id '%d' not found", compId)));
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findByIdIn(updateRequest.getEvents().stream().collect(Collectors.toList())));
            compilation.setEvents(events);
        }
        Compilation updated = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(updated, requestRepository, statsService);
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilations = pinned != null
                ? compilationRepository.findByPinned(pinned, pageable)
                : compilationRepository.findAll(pageable);
        return compilations.stream()
                .map(c -> CompilationMapper.toCompilationDto(c, requestRepository, statsService))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new RuntimeException(String.format("Compilation with id '%d' not found", compId)));
        return CompilationMapper.toCompilationDto(compilation, requestRepository, statsService);
    }
}