package com.ColumbusEventAlertService.refactor.services;

import com.ColumbusEventAlertService.refactor.exception.EventFetchException;
import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventAggregatorService {
    List<EventSourceStrategy> eventSources;

    public EventAggregatorService(List<EventSourceStrategy> eventSources) {
        this.eventSources = eventSources;
    }

    public List<Event> getCurrentDayEvents() {
        log.info("Fetching Today's Events");

        List<Event> currentDayEvents = new ArrayList<>();

        for (EventSourceStrategy source : eventSources) {
            try {
                List<Event> events = source.fetchCurrentDayEvents();

                if (!events.isEmpty()) {
                    currentDayEvents.addAll(events);
                    currentDayEvents = removeDuplicates(currentDayEvents);
                    log.info("Added {} events for {}", currentDayEvents.size(), source);
                } else {
                    log.info("No events found for {}", source);
                }
            }  catch (EventFetchException e) {
            log.error("Failed to fetch events from {}: {}",
                    source.getSourceName(), e.getMessage(), e);
            }
        }
        return currentDayEvents;
    }

    List<Event> removeDuplicates(List<Event> events) {
        record EventKey(String locationName, String name, LocalDate date) {}

        /* Creates a key based on the event location, name, and date.
        * assigns the Event as the value to that key.
        * If there's already an Event for that key, it prioritizes the Event with a time (if any) */
        return new ArrayList<>(events.stream()
                .collect(Collectors.toMap(
                        e -> new EventKey(e.getLocationName(), e.getName(), e.getDate()),
                        e -> e,
                        (existing, replacement) -> existing.getTime() != null && !existing.getTime().isBlank() ? existing : replacement
                ))
                .values());
    }

}

