package com.ColumbusEventAlertService.utils;

import com.ColumbusEventAlertService.truncated.utils.DynamoDBReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DynamoDBReaderTest {
    @InjectMocks
    private DynamoDBReader dynamoDBReader;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void databaseReturnsTodaysEvents() {
        ZoneId zone = ZoneId.of("America/New_York");
        String todaysDate = Instant.now().atZone(zone).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

        Map<String, AttributeValue> item = Map.of(
            "_airbyte_data", AttributeValue.fromM(Map.of(
                    "date", AttributeValue.fromS(todaysDate),
                    "locationName", AttributeValue.fromS("Short North"),
                    "eventName", AttributeValue.fromS("Doo Dah Parade")
            ))
        );
        ScanResponse mockResponse = ScanResponse.builder()
                .items(List.of(item))
                .build();

        when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(mockResponse);

        List<Map<String, AttributeValue>> result = dynamoDBReader.getTodaysEvents(dynamoDbClient);

        assertEquals(1, result.size());
    }
}