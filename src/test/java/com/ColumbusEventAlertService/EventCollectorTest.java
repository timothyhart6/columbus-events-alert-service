package com.ColumbusEventAlertService;

import com.ColumbusEventAlertService.truncated.EventCollector;
import com.ColumbusEventAlertService.truncated.models.Event;
import com.ColumbusEventAlertService.truncated.services.events.*;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import com.ColumbusEventAlertService.truncated.utils.DynamoDBReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventCollectorTest {
    @InjectMocks
    private EventCollector eventCollector;
    @Mock
    NationwideEventService nationwideEventService;
    @Mock
    KembaLiveEventService kembaLiveEventService;
    @Mock
    NewportEventService newportEventService;
    @Mock
    ArBarEventService arBarEventService;
    @Mock
    AceOfCupsEventService aceOfCupsEventService;
    @Mock
    ShortNorthStageService shortNorthStageService;
    @Mock
    BalletMetService balletMetService;
    @Mock
    DateUtil dateUtil;

    @Mock
    DynamoDBReader dynamoDBReader;

    private String todaysDate;

    @BeforeEach
    public void setUp() {
        ZoneId zone = ZoneId.of("America/New_York");
        todaysDate = Instant.now().atZone(zone).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
    }
    @Test
    public void testGetTodaysEvents() {
        Event currentEvent = new Event("Today!", true, false);
        Event pastEvent = new Event("In the past", true, false);
        currentEvent.setDate(todaysDate);
        pastEvent.setDate("01-01-1930");
        currentEvent.setEventName("Current!");
        pastEvent.setEventName("Past!");

        when(nationwideEventService.getNextEvent()).thenReturn(currentEvent);
        when(kembaLiveEventService.getNextEvent()).thenReturn(currentEvent);
        when(arBarEventService.getNextEvent()).thenReturn(pastEvent);
        when(newportEventService.getNextEvent()).thenReturn(pastEvent);
        when(aceOfCupsEventService.getNextEvent()).thenReturn(currentEvent);
        when(shortNorthStageService.getNextEvent()).thenReturn(pastEvent);
        when(balletMetService.getNextEvent()).thenReturn(pastEvent);
        when(dateUtil.getCurrentDateFormatted()).thenCallRealMethod();

        ArrayList<Event> todaysEvents = eventCollector.getTodaysEvents(dynamoDBReader);

        assertEquals(3, todaysEvents.size());
    }

    @Test
    void databaseEventsMapToArrayList() {
        try (MockedStatic<EventCollector> mockedStatic = mockStatic(EventCollector.class)) {
            mockedStatic.when(EventCollector::isRunningLocally).thenReturn(false);
            mockedStatic.when(() -> EventCollector.nullCheckString(any())).thenCallRealMethod();
            mockedStatic.when(() -> EventCollector.nullCheckBool(any())).thenCallRealMethod();

            List<Map<String, AttributeValue>> events = new ArrayList<>();
            Map<String, AttributeValue> event = Map.of(
                    "id", AttributeValue.builder().s("Doo Dah Parade|Short North|" + todaysDate).build(),
                    "date", AttributeValue.builder().s(todaysDate).build(),
                    "locationName", AttributeValue.builder().s("Short North").build(),
                    "eventName", AttributeValue.builder().s("Doo Dah Parade").build(),
                    "time", AttributeValue.builder().s("12:00 PM").build(),
                    "isBadTraffic", AttributeValue.builder().bool(true).build(),
                    "isDesiredEvent", AttributeValue.builder().bool(false).build()
            );

            events.add(event);

            when(dynamoDBReader.getTodaysEvents(any())).thenReturn(events);

            ArrayList<Event> todaysEvents = eventCollector.getTodaysEventsFromDatabase(dynamoDBReader);

            assertEquals(1, todaysEvents.size());
            Event retrievedEvent = todaysEvents.get(0);
            assertEquals("Short North", retrievedEvent.getLocationName());
            assertEquals("Doo Dah Parade", retrievedEvent.getEventName());
            assertEquals(todaysDate, retrievedEvent.getDate());
            assertEquals("12:00 PM", retrievedEvent.getTime());
            assertTrue(retrievedEvent.isBadTraffic());
            assertFalse(retrievedEvent.isDesiredEvent());

            verify(dynamoDBReader, times(1)).getTodaysEvents(any());
        }
    }

    @Test
    public void noEventsAddedWhenListIsEmpty() {
        ArrayList<Event> todaysEvents = eventCollector.getTodaysEventsFromDatabase(dynamoDBReader);

        assertEquals(0, todaysEvents.size());
    }

    @Test
    void combineEventLists_removesWebEventWithoutTimeWhenMatchingDbEventExists() {
        EventCollector eventCollector = new EventCollector();
        ArrayList<Event> webEvents = new ArrayList<>();
        ArrayList<Event> dbEvents = new ArrayList<>();

        Event webEvent = new Event("Venue1", "Event1", "01-01-2024", null, false, false);
        Event dbEvent = new Event("Venue1", "Event1", "01-01-2024", "7:00 PM", false, false);

        webEvents.add(webEvent);
        dbEvents.add(dbEvent);

        ArrayList<Event> result = eventCollector.combineEventLists(webEvents, dbEvents);

        assertEquals(1, result.size());
        assertEquals("7:00 PM", result.get(0).getTime());
    }

    @Test
    void combineEventLists_keepsWebEventWithTimeEvenWhenMatchingDbEventExists() {
        EventCollector eventCollector = new EventCollector();
        ArrayList<Event> webEvents = new ArrayList<>();
        ArrayList<Event> dbEvents = new ArrayList<>();

        Event webEvent = new Event("Venue1", "Event1", "01-01-2024", "8:00 PM", false, false);
        Event dbEvent = new Event("Venue1", "Event1", "01-01-2024", "7:00 PM", false, false);

        webEvents.add(webEvent);
        dbEvents.add(dbEvent);

        ArrayList<Event> result = eventCollector.combineEventLists(webEvents, dbEvents);

        assertEquals(2, result.size());
    }

    @Test
    void combineEventLists_keepsWebEventWithoutTimeWhenNoMatchingDbEvent() {
        EventCollector eventCollector = new EventCollector();
        ArrayList<Event> webEvents = new ArrayList<>();
        ArrayList<Event> dbEvents = new ArrayList<>();

        Event webEvent = new Event("Venue1", "Event1", "01-01-2024", null, false, false);
        Event dbEvent = new Event("Venue2", "Event2", "01-01-2024", "7:00 PM", false, false);

        webEvents.add(webEvent);
        dbEvents.add(dbEvent);

        ArrayList<Event> result = eventCollector.combineEventLists(webEvents, dbEvents);

        assertEquals(2, result.size());
    }
}