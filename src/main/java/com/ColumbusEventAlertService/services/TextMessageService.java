package com.ColumbusEventAlertService.services;

import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.services.EventAggregatorService;
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
        List<com.ColumbusEventAlertService.refactor.models.Event> events = eventAggregatorService.getCurrentDayEvents();
        String textMessage = formatTodaysTextMessage(events);
        twilioService.sendTextMessage(textMessage);
    }

    private String formatTodaysTextMessage(List<com.ColumbusEventAlertService.refactor.models.Event> events) {
        if (events.isEmpty()) {
            log.info("No events to send");
            return "No Events today!";
        }

        ArrayList<com.ColumbusEventAlertService.refactor.models.Event> trafficCausingEvents = new ArrayList<>();
        ArrayList<com.ColumbusEventAlertService.refactor.models.Event> interestingEvents = new ArrayList<>();

        for (com.ColumbusEventAlertService.refactor.models.Event event : events) {
            if (event.isTrafficCausing()) trafficCausingEvents.add(event);
            if (event.isInteresting()) interestingEvents.add(event);
        }

        String trafficCausingMessage = createTrafficCausingMessage(trafficCausingEvents, "- %s: %s");

        String interestingEventsMessage = getInterestingEventsMessage(interestingEvents, "- %s\n%s\n%s");

        // Combine both messages.
        String completeMessage = """
    %s

    %s
    """.formatted(trafficCausingMessage, interestingEventsMessage);

        log.info("Formatted Text Message being sent: " + completeMessage);
        return completeMessage;
    }

    private static String createTrafficCausingMessage(ArrayList<com.ColumbusEventAlertService.refactor.models.Event> trafficCausingEvents, String format) {
        if (trafficCausingEvents.isEmpty()) {
            return "Smooth sailing today! No events causing major traffic concerns.";
        }

        String titleText = "AHHH TRAFFIC ALERT!!\n";
        String trafficCausingMessage = trafficCausingEvents.stream()
                .map(event -> String.format(format,
                        event.getLocationName(), event.getName(), event.getTime()))
                .collect(Collectors.joining("\n\n")); // Adds an extra newline between each event
        return titleText + trafficCausingMessage;
    }

    private static String getInterestingEventsMessage(ArrayList<Event> interestingEvents, String format) {
        if (interestingEvents.isEmpty()) {
            return "No reason to leave home today!";
        }
        String titleText = "THESE SHOWS MIGHT BE FUN!\n";
        String interestingEventsMessage = interestingEvents.stream()
                .map(event -> String.format(format,
                        event.getName(), event.getLocationName(), event.getTime()))
                .collect(Collectors.joining("\n\n"));
        return titleText + interestingEventsMessage;
    }
}