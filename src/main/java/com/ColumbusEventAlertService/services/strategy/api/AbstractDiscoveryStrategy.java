package com.ColumbusEventAlertService.services.strategy.api;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import com.ColumbusEventAlertService.services.strategy.EventSourceStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class AbstractDiscoveryStrategy implements EventSourceStrategy {

    private final String venueName;
    private final String venueId;

    @Value("${api.discovery.key}")
    private String apiKey;

    @Value("${api.discovery.url}")
    private String url;

    private final HttpClient httpClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AbstractDiscoveryStrategy(String venueName, String venueId) {
        this(venueName, venueId, HttpClient.newHttpClient());
    }

    AbstractDiscoveryStrategy(String venueName, String venueId, HttpClient httpClient) {
        this.venueName = venueName;
        this.venueId = venueId;
        this.httpClient = httpClient;
    }

    @Override
    public List<Event> fetchCurrentDayEvents() throws EventFetchException {
        log.info("Fetching today's events for {}", venueName);

        String requestUrl = url
                .replace("{VENUE_ID}", venueId)
                .replace("{API_KEY}", apiKey);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new EventFetchException(
                        venueName,
                        EventFetchException.ErrorType.NETWORK_ERROR,
                        "Discovery API returned status " + response.statusCode()
                );
            }

            return parseEvents(response.body());

        } catch (EventFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new EventFetchException(
                    venueName,
                    EventFetchException.ErrorType.NETWORK_ERROR,
                    "Failed to connect to Discovery API",
                    e
            );
        }
    }

    private List<Event> parseEvents(String responseBody) throws EventFetchException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode eventsNode = root.path("_embedded").path("events");

            if (eventsNode.isMissingNode() || !eventsNode.isArray()) {
                log.info("No events found for {}", venueName);
                return Collections.emptyList();
            }

            List<Event> todayEvents = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (JsonNode eventNode : eventsNode) {
                String name = eventNode.path("name").asText();
                JsonNode start = eventNode.path("dates").path("start");
                LocalDate eventDate = LocalDate.parse(start.path("localDate").asText());
                String rawTime = start.path("localTime").asText(null);
                String localTime = rawTime != null
                        ? LocalTime.parse(rawTime).format(DateTimeFormatter.ofPattern("h:mm a"))
                        : null;

                if (eventDate.equals(today)) {
                    todayEvents.add(Event.builder()
                            .name(name)
                            .locationName(venueName)
                            .date(eventDate)
                            .time(localTime)
                            .build());
                }
            }

            log.info("Found {} events today for {}", todayEvents.size(), venueName);
            return todayEvents;

        } catch (Exception e) {
            throw new EventFetchException(
                    venueName,
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Failed to parse Discovery API response",
                    e
            );
        }
    }

    @Override
    public String getSourceName() {
        return venueName;
    }
}