package com.ColumbusEventAlertService.truncated.utils;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DynamoDBReader {

    public List<Map<String, AttributeValue>> getTodaysEvents(DynamoDbClient dynamoDbClient) {
        ZoneId zone = ZoneId.of("America/New_York");
        String todaysDate = Instant.now().atZone(zone).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

        // Create the scan request with a filter expression to match today's date
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName("ColumbusEvents")
                .filterExpression("#date = :todayDate")
                .expressionAttributeNames(Map.of(
                        "#date", "date" // aliasing because "date" is reserved
                ))
                .expressionAttributeValues(Map.of(
                        ":todayDate", AttributeValue.builder().s(todaysDate).build()
                ))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        List<Map<String, AttributeValue>> items = scanResponse.items();

        dynamoDbClient.close();
        return items;
    }
}
