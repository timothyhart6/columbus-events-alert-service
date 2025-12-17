package com.ColumbusEventAlertService.refactor.services;

import com.ColumbusEventAlertService.models.Event;
import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EventAggregatorService {

    public List<Event> getTodaysEvents() {
        log.info("Fetching Today's Events");
        //todo get all sources
        List<EventSourceStrategy> eventSources = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        for (EventSourceStrategy source : eventSources) {

            try {
                List<Event> todaysEvents = source.getTodaysEvents();

                if (!todaysEvents.isEmpty()) {
                    events.addAll(todaysEvents);
                    log.info("Added {} events for {}", events.size(), source);
                } else
                    log.info("No events found for {}", source);

            } catch(Exception e){
                log.error("Failed to fetch Events from {}: {}", source.getName(), e.getMessage(), e);
            }
        }
        return removeDuplicates(events);
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
