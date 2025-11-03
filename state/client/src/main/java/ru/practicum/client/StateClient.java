package ru.practicum.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class StateClient {
    private final RestTemplate restTemplate;
    private final String url;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String app;

    public StateClient(@Value("${stats.server.url:http://stats-server:9090}") String serverUrl,
                       @Value("${spring.application.name:ewm-service}") String appName) {
        this.restTemplate = new RestTemplate();
        this.url = serverUrl;
        this.app = appName;
    }

    public void hit(HttpServletRequest httpServletRequest) {
        EndpointHitDto endpointHitDto = new EndpointHitDto(null, app, httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(),LocalDateTime.now().toString());
        restTemplate.postForLocation(url + "/hit", endpointHitDto);
    }

    public List<ViewStats> stats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromHttpUrl(url + "/stats")
                .queryParam("start", start.format(formatter))
                .queryParam("end", end.format(formatter))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uriComponents.queryParam("uris", String.join(",", uris));
        }

        String finalUrl = uriComponents.encode().toUriString();

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(finalUrl, ViewStats[].class);
        return Optional.ofNullable(response.getBody())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
