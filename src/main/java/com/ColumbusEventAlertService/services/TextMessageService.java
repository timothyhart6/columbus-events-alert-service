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
        String textMessage = events.isEmpty() ? "No Events today!" : formatTodaysTextMessage(events);
//        twilioService.sendTextMessage(textMessage);
    }

    private String formatTodaysTextMessage(List<com.ColumbusEventAlertService.refactor.models.Event> events) {
        ArrayList<com.ColumbusEventAlertService.refactor.models.Event> badTrafficEvents = new ArrayList<>();
        ArrayList<com.ColumbusEventAlertService.refactor.models.Event> desiredEvents = new ArrayList<>();

        for (com.ColumbusEventAlertService.refactor.models.Event event : events) {
            if (event.isTrafficCausing()) badTrafficEvents.add(event);
            if (event.isInteresting()) desiredEvents.add(event);
        }

        // Adjusted format string for bad traffic message to match two arguments.
        String badTrafficMessage = getBadTrafficMessage(badTrafficEvents, "- %s: %s");

        String funEventsMessage = getFunEventsMessage(desiredEvents, "- %s\n%s\n%s");

        // Combine both messages.
        String completeMessage = """
    %s

    %s
    """.formatted(badTrafficMessage, funEventsMessage);

        log.info("Formatted Text Message being sent: " + completeMessage);
        return completeMessage;
    }

    private static String getBadTrafficMessage(ArrayList<com.ColumbusEventAlertService.refactor.models.Event> badTrafficEvents, String format) {
        if (badTrafficEvents.isEmpty()) {
            return "Smooth sailing today! No events causing major traffic concerns.";
        }

        String titleText = "AHHH TRAFFIC ALERT!!\n";
        String badTrafficMessage = badTrafficEvents.stream()
                .map(event -> String.format(format,
                        event.getLocationName(), event.getName(), event.getTime()))
                .collect(Collectors.joining("\n\n")); // Adds an extra newline between each event
        return titleText + badTrafficMessage;
    }

    private static String getFunEventsMessage(ArrayList<Event> desiredEvents, String format) {
        if (desiredEvents.isEmpty()) {
            return "No reason to leave home today!";
        }
        String titleText = "THESE SHOWS MIGHT BE FUN!\n";
        String funEventsMessage = desiredEvents.stream()
                .map(event -> String.format(format,
                        event.getName(), event.getLocationName(), event.getTime()))
                .collect(Collectors.joining("\n\n"));
        return titleText + funEventsMessage;
    }
}