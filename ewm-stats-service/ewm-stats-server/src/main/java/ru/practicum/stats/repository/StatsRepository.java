package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHitEntity, Long> {

    @Query("SELECT new ru.practicum.stats.dto.ViewStats(e.app, e.uri, COUNT(e.id)) " +
           "FROM EndpointHitEntity e " +
           "WHERE e.timestamp BETWEEN :start AND :end " +
           "AND (:uris IS NULL OR e.uri IN :uris) " +
           "GROUP BY e.app, e.uri " +
           "ORDER BY COUNT(e.id) DESC")
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.stats.dto.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
           "FROM EndpointHitEntity e " +
           "WHERE e.timestamp BETWEEN :start AND :end " +
           "AND (:uris IS NULL OR e.uri IN :uris) " +
           "GROUP BY e.app, e.uri " +
           "ORDER BY COUNT(DISTINCT e.ip) DESC")
    List<ViewStats> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}