package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StateClient {
    private final RestTemplate restTemplate;
    private final String url;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StateClient(String serverUrl) {
        this.restTemplate = new RestTemplate();
        this.url = serverUrl;
    }

    public void hit(EndpointHitDto endpointHitDto) {
        restTemplate.postForLocation(url + "/hit", endpointHitDto);
    }

    public List<ViewStats> stats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start.format(formatter));
        parameters.put("end", end.format(formatter));
        parameters.put("unique", unique);

        String url = this.url + "/stats?start={start}&end={end}&unique={unique}";

        if (uris != null && !uris.isEmpty()) {
            url += "&uris={uris}";
            parameters.put("uris", String.join(",", uris));
        }

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(url, ViewStats[].class, parameters);
        return Arrays.asList(response.getBody());
    }
}
