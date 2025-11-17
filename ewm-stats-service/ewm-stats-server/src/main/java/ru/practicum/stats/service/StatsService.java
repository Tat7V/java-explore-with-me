package ru.practicum.stats.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.EndpointHitEntity;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatsService {

    StatsRepository statsRepository;

    @Transactional
    public void saveHit(EndpointHit endpointHit) {
        EndpointHitEntity entity = new EndpointHitEntity();
        entity.setApp(endpointHit.getApp());
        entity.setUri(endpointHit.getUri());
        entity.setIp(endpointHit.getIp());
        entity.setTimestamp(endpointHit.getTimestamp());
        statsRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris != null && uris.isEmpty()) {
            return List.of();
        }
        
        if (Boolean.TRUE.equals(unique)) {
            return statsRepository.getUniqueStats(start, end, uris);
        } else {
            return statsRepository.getStats(start, end, uris);
        }
    }
}