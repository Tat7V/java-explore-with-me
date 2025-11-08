package ru.practicum.stats.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatsClient {
    RestTemplate restTemplate;
    StatsClientConfig config;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(StatsClientConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    public void hit(String uri, String ip, LocalDateTime timestamp) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(config.getAppName());
        endpointHit.setUri(uri);
        endpointHit.setIp(ip);
        endpointHit.setTimestamp(timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHit> request = new HttpEntity<>(endpointHit, headers);

        try {
            restTemplate.postForEntity(
                    config.getServerUrl() + "/hit",
                    request,
                    Void.class
            );
        } catch (Exception e) {
            log.warn("Не удалось отправить hit в сервис статистики: {}", e.getMessage());
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            String encodedStart = URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8);
            String encodedEnd = URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8);

            StringBuilder urlBuilder = new StringBuilder(config.getServerUrl() + "/stats");
            urlBuilder.append("?start=").append(encodedStart);
            urlBuilder.append("&end=").append(encodedEnd);

            if (uris != null && !uris.isEmpty()) {
                for (String uri : uris) {
                    urlBuilder.append("&uris=").append(URLEncoder.encode(uri, StandardCharsets.UTF_8));
                }
            }

            if (Boolean.TRUE.equals(unique)) {
                urlBuilder.append("&unique=true");
            }

            ResponseEntity<ViewStats[]> response = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.GET,
                    null,
                    ViewStats[].class
            );

            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.warn("Не удалось получить статистику: {}", e.getMessage());
            return List.of();
        }
    }
}