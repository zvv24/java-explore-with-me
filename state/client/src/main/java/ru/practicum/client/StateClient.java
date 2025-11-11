package ru.practicum.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class StateClient {
    private final RestTemplate restTemplate;
    private final String url;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StateClient(@Value("${stats.server.url:http://localhost:9090}") String serverUrl) {
        this.url = serverUrl;
        this.restTemplate = new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public void hit(HttpServletRequest httpServletRequest) {
        String appName = "ewm-service";
        EndpointHitDto endpointHitDto = new EndpointHitDto(null,
                appName,
                httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(),
                LocalDateTime.now().format(formatter));
        try {
            restTemplate.postForLocation(url + "/hit", endpointHitDto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<ViewStats> stats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String formattedStart = start.format(formatter);
        String formattedEnd = end.format(formatter);

        String urisParam = "";
        if (uris != null && !uris.isEmpty()) {
            urisParam = "&uris=" + String.join(",", uris);
        }

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(
                url + "/stats" + "?start=" + formattedStart + "&end=" + formattedEnd +
                        urisParam + "&unique=" + unique,
                ViewStats[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }
}
