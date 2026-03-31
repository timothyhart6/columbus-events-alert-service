package com.ColumbusEventAlertService.services;

import com.ColumbusEventAlertService.models.Event;
import com.ColumbusEventAlertService.services.smsProviders.TwilioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TextMessageService {
    @Autowired
    private TwilioService twilioService;
    @Autowired
    private EventAggregatorService eventAggregatorService;

    //Method that sends the Text Message
    public void sendTodaysEvents() {
        log.info("Text Message is sending...");
        List<Event> events = eventAggregatorService.getCurrentDayEvents();
        String textMessage = formatTodaysTextMessage(events);
        twilioService.sendTextMessage(textMessage);
    }

    private String formatTodaysTextMessage(List<Event> events) {
        if (events.isEmpty()) {
            log.info("No events to send");
            return "No Events today!";
        }

        ArrayList<Event> trafficCausingEvents = new ArrayList<>();
        ArrayList<Event> interestingEvents = new ArrayList<>();

        for (Event event : events) {
            if (event.isTrafficCausing()) trafficCausingEvents.add(event);
            if (event.isInteresting()) interestingEvents.add(event);
        }

        String trafficCausingMessage = createTrafficCausingMessage(trafficCausingEvents);

        String interestingEventsMessage = getInterestingEventsMessage(interestingEvents);

        // Combine both messages.
        String completeMessage = """
    %s

    -------------------------
    
    %s
    """.formatted(trafficCausingMessage, interestingEventsMessage);

        log.info("Formatted Text Message being sent: " + completeMessage);
        return completeMessage;
    }

    private static String createTrafficCausingMessage(ArrayList<Event> trafficCausingEvents) {
        if (trafficCausingEvents.isEmpty()) {
            return "No traffic causing events today.";
        }

        String titleText = "TRAFFIC CAUSING EVENTS:\n\n";
        String trafficCausingMessage = trafficCausingEvents.stream()
                .map(event -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("😠 ").append(event.getName()).append("\n");
                    sb.append("📍 ").append(event.getLocationName());
                    if (event.getTime() != null && !event.getTime().isBlank()) {
                        sb.append("\n🕐 ").append(event.getTime());
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining("\n\n"));
        return titleText + trafficCausingMessage;
    }

    private static String getInterestingEventsMessage(ArrayList<Event> interestingEvents) {
        if (interestingEvents.isEmpty()) {
            return "No known interesting events today";
        }
        String titleText = "INTERESTING EVENTS:\n\n";
        String interestingEventsMessage = interestingEvents.stream()
                .map(event -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("🎸 ").append(event.getName()).append("\n");
                    sb.append("📍 ").append(event.getLocationName());
                    if (event.getTime() != null && !event.getTime().isBlank()) {
                        sb.append("\n🕐 ").append(event.getTime());
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining("\n\n"));
        return titleText + interestingEventsMessage;
    }
}