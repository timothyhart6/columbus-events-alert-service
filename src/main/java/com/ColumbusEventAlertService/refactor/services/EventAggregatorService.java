package com.ColumbusEventAlertService.refactor.services;

import com.ColumbusEventAlertService.refactor.exception.EventFetchException;
import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                    log.info("Added {} events for {}", currentDayEvents.size(), source);
                } else {
                    log.info("No events found for {}", source);
                }
            }  catch (EventFetchException e) {
            log.error("Failed to fetch events from {}: {}",
                    source.getLocationName(), e.getMessage(), e);
            }
        }
        return currentDayEvents;
    }

    private List<Event> removeDuplicates(List<Event> events) {
        //todo determine which logic to use
        //ArrayList<> distinctEvents = new LinkedHashSet<>(allEvents);
        return new ArrayList<>(events);
    }

}

/*
* TODO
*  1. Create EventSourceStrategy
*  2. implement EventSourceStrategy for the Event class
*  3.Create method to get all events
*  4. Override the equals() and hashCode() methods in Events
*  5. put it all together
* */
