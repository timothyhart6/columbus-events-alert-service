package com.ColumbusEventAlertService.truncated;

import com.ColumbusEventAlertService.truncated.models.Event;
import com.ColumbusEventAlertService.truncated.services.events.*;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import com.ColumbusEventAlertService.truncated.utils.DynamoDBReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EventCollector {
    @Autowired
    NationwideEventService nationwideEventService;
    @Autowired
    KembaLiveEventService kembaLiveEventService;
    @Autowired
    NewportEventService newportEventService;
    @Autowired
    ArBarEventService arBarEventService;
    @Autowired
    AceOfCupsEventService aceOfCupsEventService;
    @Autowired
    ShortNorthStageService shortNorthStageService;
    @Autowired
    BalletMetService balletMetService;
    @Autowired
    private DateUtil dateUtil;

    public ArrayList<Event> getTodaysEvents(DynamoDBReader dynamoDBReader) {

        ArrayList<Event> todaysEventsFromDatabase = getTodaysEventsFromDatabase(dynamoDBReader);
        ArrayList<Event> todaysEventsFromWebScrape = getTodaysEventsFromWebScrape();

        return combineEventLists(todaysEventsFromWebScrape, todaysEventsFromDatabase);
    }

    ArrayList<Event> combineEventLists(ArrayList<Event> todaysEventsFromWebScrape, ArrayList<Event> todaysEventsFromDatabase) {
        ArrayList<Event> events = new ArrayList<>();
        todaysEventsFromWebScrape.removeIf(webEvent ->
            (webEvent.getTime() == null || webEvent.getTime().isEmpty()) &&
            todaysEventsFromDatabase.stream().anyMatch(dbEvent ->
                webEvent.getLocationName().equals(dbEvent.getLocationName()) &&
                webEvent.getEventName().equals(dbEvent.getEventName()) &&
                webEvent.getDate().equals(dbEvent.getDate())
            )
        );
        events.addAll(todaysEventsFromWebScrape);
        events.addAll(todaysEventsFromDatabase);

        return events;
    }

    private ArrayList<Event> getTodaysEventsFromWebScrape() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(nationwideEventService.getNextEvent());
        events.add(kembaLiveEventService.getNextEvent());
        events.add(newportEventService.getNextEvent());
        events.add(arBarEventService.getNextEvent());
        events.add(aceOfCupsEventService.getNextEvent());
        events.add(shortNorthStageService.getNextEvent());
        events.add(balletMetService.getNextEvent());
        events.removeIf(event -> (!shouldSendEventToday(event)));

        return events;
    }

    private boolean shouldSendEventToday(Event event) {
        boolean validEventName = event.getEventName() != null && !event.getEventName().isEmpty();
        boolean validLocationName = event.getLocationName() != null && !event.getLocationName().isEmpty();
        boolean currentDate = event.getDate() != null && !event.getDate().isEmpty() && event.getDate().equals(dateUtil.getCurrentDateFormatted());
        return validEventName && validLocationName && currentDate;
    }

    public ArrayList<Event> getTodaysEventsFromDatabase(DynamoDBReader dynamoDBReader) {
        ArrayList<Event> events = new ArrayList<>();

        // Skip DynamoDB calls when running locally
        if (isRunningLocally()) {
            return events;
        }

        List<Map<String, AttributeValue>> items = dynamoDBReader.getTodaysEvents(DynamoDbClientFactory.createClient());

        if (items.isEmpty()) {
            return events;
        } else {
            for (Map<String, AttributeValue> item : items) {
                String locationName = nullCheckString(item.get("locationName"));
                String eventName = nullCheckString(item.get("eventName"));
                String date = nullCheckString(item.get("date"));
                String time = nullCheckString(item.get("time"));
                boolean createsTraffic = nullCheckBool(AttributeValue.fromBool(item.get("isBadTraffic").bool()));
                boolean desiredEvent = nullCheckBool(AttributeValue.fromBool(item.get("isDesiredEvent").bool()));

                Event event = new Event(locationName, eventName, date, time, createsTraffic, desiredEvent);
                events.add(event);
            }
        }
        return events;
    }

    static String nullCheckString(AttributeValue attribute) {
        return (attribute != null && attribute.s() != null) ? attribute.s() : "";
    }

    static boolean nullCheckBool(AttributeValue attribute) {
        return attribute != null && attribute.bool() != null ? attribute.bool() : true;
    }

    static boolean isRunningLocally() {
        return System.getenv("AWS_EXECUTION_ENV") == null;
    }

}