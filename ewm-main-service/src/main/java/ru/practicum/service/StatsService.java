package ru.practicum.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.client.StatsClientConfig;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsService {
    final StatsClient statsClient;

    public StatsService(@Value("${stats.server.url}") String serverUrl,
                        @Value("${stats.app.name}") String appName) {
        StatsClientConfig config = new StatsClientConfig(serverUrl, appName);
        this.statsClient = new StatsClient(config);
    }

    public void saveHit(String uri, String ip) {
        statsClient.hit(uri, ip, LocalDateTime.now());
    }

    public List<ru.practicum.stats.dto.ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return statsClient.getStats(start, end, uris, unique);
    }
}