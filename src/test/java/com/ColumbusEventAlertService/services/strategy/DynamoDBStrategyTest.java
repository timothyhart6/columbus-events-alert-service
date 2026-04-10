package com.ColumbusEventAlertService.services.strategy;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DynamoDBStrategyTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoDBStrategy strategy;

    private static final String TABLE_NAME = "test-events-table";

    @BeforeEach
    void setUp() {
        strategy = spy(new DynamoDBStrategy(dynamoDbClient, TABLE_NAME));
    }

    @Test
    void mapEvent_mapsAllFields_whenItemIsComplete() throws EventFetchException {
        Map<String, AttributeValue> item = buildItem(
                "Nationwide Arena", "Hockey Game", "06-15-2025", "19:00", true, false
        );

        Event event = strategy.mapEvent(item);

        assertEquals("Nationwide Arena", event.getLocationName());
        assertEquals("Hockey Game", event.getName());
        assertEquals(LocalDate.of(2025, 6, 15), event.getDate());
        assertEquals("19:00", event.getTime());
        assertTrue(event.isTrafficCausing());
        assertFalse(event.isInteresting());
    }

    @Test
    void mapEvent_defaultsToTrue_whenTrafficAndInterestingFieldsMissing() throws EventFetchException {
        Map<String, AttributeValue> item = buildItem(
                "Venue", "Concert", "06-15-2025", null, null, null
        );

        Event event = strategy.mapEvent(item);

        assertTrue(event.isTrafficCausing());
        assertTrue(event.isInteresting());
    }

    @Test
    void mapEvent_throwsEventFetchException_whenLocationNameMissing() {
        Map<String, AttributeValue> item = buildItem(
                null, "Hockey Game", "06-15-2025", "19:00", true, false
        );

        EventFetchException ex = assertThrows(EventFetchException.class,
                () -> strategy.mapEvent(item));
        assertEquals(EventFetchException.ErrorType.PARSING_ERROR, ex.getErrorType());
    }

    @Test
    void mapEvent_throwsEventFetchException_whenEventNameMissing() {
        Map<String, AttributeValue> item = buildItem(
                "Nationwide Arena", null, "06-15-2025", "19:00", true, false
        );

        assertThrows(EventFetchException.class, () -> strategy.mapEvent(item));
    }



    @Test
    void mapEvent_throwsEventFetchException_whenDateMissing() {
        Map<String, AttributeValue> item = buildItem(
                "Nationwide Arena", "Hockey Game", null, "19:00", true, false
        );

        assertThrows(EventFetchException.class, () -> strategy.mapEvent(item));
    }

    @Test
    void mapEvent_throwsEventFetchException_whenDateMalformed() {
        Map<String, AttributeValue> item = buildItem(
                "Nationwide Arena", "Hockey Game", "06/15/2025", "19:00", true, false
        );

        EventFetchException ex = assertThrows(EventFetchException.class,
                () -> strategy.mapEvent(item));
        assertEquals(EventFetchException.ErrorType.PARSING_ERROR, ex.getErrorType());
    }

    @Test
    void fetchCurrentDayEvents_returnsMultipleEvents_whenMultipleItemsFound() throws EventFetchException {
        doReturn(false).when(strategy).isRunningLocally();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        Map<String, AttributeValue> item1 = buildItem(
                "Nationwide Arena", "Hockey Game", today, "19:00", true, false
        );
        Map<String, AttributeValue> item2 = buildItem(
                "Schottenstein Center", "Concert", today, "20:00", false, true
        );

        when(dynamoDbClient.scan(any(ScanRequest.class)))
                .thenReturn(ScanResponse.builder().items(List.of(item1, item2)).build());

        List<Event> result = strategy.fetchCurrentDayEvents();

        assertEquals(2, result.size());
        assertEquals("Hockey Game", result.get(0).getName());
        assertEquals("Concert", result.get(1).getName());
    }

    @Test
    void fetchCurrentDayEvents_returnsEmptyList_whenNoItemsFound() throws EventFetchException {
        doReturn(false).when(strategy).isRunningLocally();
        when(dynamoDbClient.scan(any(ScanRequest.class)))
                .thenReturn(ScanResponse.builder().items(List.of()).build());

        assertTrue(strategy.fetchCurrentDayEvents().isEmpty());
    }

    @Test
    void fetchCurrentDayEvents_throwsEventFetchException_onDynamoDbException() {
        doReturn(false).when(strategy).isRunningLocally();
        when(dynamoDbClient.scan(any(ScanRequest.class)))
                .thenThrow(DynamoDbException.builder().message("timeout").build());

        EventFetchException ex = assertThrows(EventFetchException.class,
                () -> strategy.fetchCurrentDayEvents());
        assertEquals(EventFetchException.ErrorType.DATA_SOURCE_UNAVAILABLE, ex.getErrorType());
    }

    @Test
    void parseDate_returnsCorrectDate_whenValidMMddyyyyFormat() throws EventFetchException {
        LocalDate result = DynamoDBStrategy.parseDate("06-15-2025");
        assertEquals(LocalDate.of(2025, 6, 15), result);
    }

    @Test
    void parseDate_throwsEventFetchException_whenInvalidFormat() {
        EventFetchException ex = assertThrows(EventFetchException.class,
                () -> DynamoDBStrategy.parseDate("2025-06-15"));
        assertEquals(EventFetchException.ErrorType.PARSING_ERROR, ex.getErrorType());
    }

    @Test
    void parseDate_throwsEventFetchException_whenGibberish() {
        assertThrows(EventFetchException.class,
                () -> DynamoDBStrategy.parseDate("not-a-date"));
    }


    @Test
    void nullCheckBool_returnsTrue_whenAttributeIsNull() {
        assertTrue(DynamoDBStrategy.nullCheckBool(null));
    }

    @Test
    void nullCheckBool_returnsTrue_whenBoolIsTrue() {
        assertTrue(DynamoDBStrategy.nullCheckBool(AttributeValue.builder().bool(true).build()));
    }

    @Test
    void nullCheckBool_returnsFalse_whenBoolIsFalse() {
        assertFalse(DynamoDBStrategy.nullCheckBool(AttributeValue.builder().bool(false).build()));
    }

    @Test
    void getSourceName_returnsExpectedString() {
        assertEquals("ColumbusEvents - DynamoDB table", strategy.getSourceName());
    }

    private Map<String, AttributeValue> buildItem(
            String locationName, String eventName, String date,
            String time, Boolean trafficCausing, Boolean interesting) {

        Map<String, AttributeValue> item = new HashMap<>();
        if (locationName != null)
            item.put("locationName", AttributeValue.builder().s(locationName).build());
        if (eventName != null)
            item.put("eventName", AttributeValue.builder().s(eventName).build());
        if (date != null)
            item.put("date", AttributeValue.builder().s(date).build());
        if (time != null)
            item.put("time", AttributeValue.builder().s(time).build());
        if (trafficCausing != null)
            item.put("isBadTraffic", AttributeValue.builder().bool(trafficCausing).build());
        if (interesting != null)
            item.put("isDesiredEvent", AttributeValue.builder().bool(interesting).build());
        return item;
    }

}
