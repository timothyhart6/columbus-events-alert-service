package com.ColumbusEventAlertService.refactor.services.strategy;

import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
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

public class DynamoDBStrategy implements EventSourceStrategy {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;


    public DynamoDBStrategy(
            DynamoDbClient dynamoDbClient,
            @Value("${dynamodb.events.table}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public List<Event> fetchCurrentDayEvents() throws Exception {
        List<Event> events = new ArrayList<>();

        // Skip DynamoDB calls when running locally
        if (isRunningLocally()) {
            return events;
        }

        List<Map<String, AttributeValue>> items = scanDBForCurrentDayEvents();

        if (items.isEmpty()) {
            return events;
        } else {
            for (Map<String, AttributeValue> item : items) {
                try {
                    Event event = Event.builder()
                            //do I really need all these null checks? Should I just catch exceptions instead?
                            .locationName(String.valueOf(Optional.ofNullable(item.get("locationName"))))
                            .name(nullCheckString(item.get("eventName")))
                            .date(LocalDate.parse(nullCheckString(item.get("date"))))
                            .time(nullCheckString(item.get("time")))
                            //change table column names
                            .causesTraffic(nullCheckBool(AttributeValue.fromBool(item.get("isBadTraffic").bool())))
                            .interesting(nullCheckBool(AttributeValue.fromBool(item.get("isDesiredEvent").bool())))
                            .build();
                    events.add(event);
                } catch (NullPointerException e) {

                }
            }
        }
        return events;
    }

    @Override
    public String getLocationName() {
        return "";
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

    static boolean isRunningLocally() {
        return System.getenv("AWS_EXECUTION_ENV") == null;
    }

    static String nullCheckString(AttributeValue attribute) {
        return (attribute != null && attribute.s() != null) ? attribute.s() : "";
    }

    static boolean nullCheckBool(AttributeValue attribute) {
        return attribute != null && attribute.bool() != null ? attribute.bool() : true;
    }
}
