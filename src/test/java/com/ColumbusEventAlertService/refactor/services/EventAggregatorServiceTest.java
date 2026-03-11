package com.ColumbusEventAlertService.refactor.services;

import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = {
        org.springframework.cloud.function.serverless.web.ServerlessAutoConfiguration.class
})
class EventAggregatorServiceTest {

    private EventAggregatorService service;

    @Autowired
    private List<EventSourceStrategy> eventSources;

    @Test
    void shouldLoadAllEventSources() {
        // Verify Spring found all your strategies
        assertEquals(2, eventSources.size());
    }
}