package com.ColumbusEventAlertService.refactor.services.strategy;

import com.ColumbusEventAlertService.refactor.exception.EventFetchException;
import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class DynamoDBStrategy implements EventSourceStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;


    public DynamoDBStrategy(
            DynamoDbClient dynamoDbClient,
            @Value("${dynamodb.events.table}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public List<Event> fetchCurrentDayEvents() throws EventFetchException {
        log.info("Checking DynamoDB for all events happening today");
        List<Event> events = new ArrayList<>();

        try {
            // Skip DynamoDB calls when running locally
            if (isRunningLocally()) return events;

            List<Map<String, AttributeValue>> items = scanDBForCurrentDayEvents();

            if (items.isEmpty()) return events;

            for (Map<String, AttributeValue> item : items) {
                try {
                    Event event = mapEvent(item);
                    events.add(event);
                } catch (EventFetchException e) {
                    log.error("Failed to parse event from DynamoDB: {}", e.getMessage());
                }
            }
            return events;

        } catch (DynamoDbException e) {
            throw new EventFetchException(
                    "DynamoDB",
                    EventFetchException.ErrorType.DATA_SOURCE_UNAVAILABLE,
                    "Failed to query DynamoDB: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            throw new EventFetchException(
                    "DynamoDB",
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Failed to parse DynamoDB items",
                    e
            );
        }

    }

    Event mapEvent(Map<String, AttributeValue> item) throws EventFetchException {
        String locationName =  getRequiredString(item,"locationName");
        String eventName = getRequiredString(item,"eventName");
        String dateString = getRequiredString(item,"date");
        String eventTime = Optional.ofNullable(item.get("time"))
                .map(AttributeValue::s)
                .filter(s -> !s.isEmpty())
                .orElse(null);
        //TODO change column names in dynamoDB table
        boolean trafficCausing = nullCheckBool(item.get("isBadTraffic"));
        boolean interesting = nullCheckBool(item.get("isDesiredEvent"));

        LocalDate eventDate = parseDate(dateString);

        return  Event.builder()
                .locationName(locationName)
                .name(eventName)
                .date(eventDate)
                .time(eventTime)
                .trafficCausing(trafficCausing)
                .interesting(interesting)
                .build();
    }

    static LocalDate parseDate(String dateString) throws EventFetchException {

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            throw new EventFetchException(
                    "DynamoDB",
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Invalid date format in DynamoDB: " + dateString,
                    e
            );
        }
    }

    @Override
    public String getSourceName() {
        return "ColumbusEvents - DynamoDB table";
    }

    public List<Map<String, AttributeValue>> scanDBForCurrentDayEvents() {
        ZoneId zone = ZoneId.of("America/New_York");
        String stringCurrentDate = Instant.now().atZone(zone).format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Create the scan request with a filter expression to match today's date
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("#date = :todayDate")
                .expressionAttributeNames(Map.of(
                        "#date", "date" // aliasing because "date" is reserved
                ))
                .expressionAttributeValues(Map.of(
                        ":todayDate", AttributeValue.builder().s(stringCurrentDate).build()
                ))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        List<Map<String, AttributeValue>> items = scanResponse.items();

        dynamoDbClient.close();
        return items;
    }

    boolean isRunningLocally() {
        return System.getenv("AWS_EXECUTION_ENV") == null;
    }

    static boolean nullCheckBool(AttributeValue attribute) {
        return attribute == null || attribute.bool();
    }

    private String getRequiredString(Map<String, AttributeValue> item, String key) throws EventFetchException {
        AttributeValue value = item.get(key);
        if (value == null || value.s() == null || value.s().isEmpty()) {
            throw new EventFetchException(
                    "DynamoDB",
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Required field '" + key + "' is missing or empty"
            );
        }
        return value.s();
    }
}
