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
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

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
    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";

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
            String url = String.format("%s%s", config.getServerUrl(), HIT_ENDPOINT);
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            log.warn("Failed to send hit to statistics service: {}", e.getMessage());
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(String.format("%s%s", config.getServerUrl(), STATS_ENDPOINT))
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                for (String uri : uris) {
                    builder.queryParam("uris", uri);
                }
            }

            if (Boolean.TRUE.equals(unique)) {
                builder.queryParam("unique", true);
            }

            ResponseEntity<ViewStats[]> response = restTemplate.exchange(
                    builder.build(true).toUriString(),
                    HttpMethod.GET,
                    null,
                    ViewStats[].class
            );

            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to get statistics: {}", e.getMessage());
            return List.of();
        }
    }
}