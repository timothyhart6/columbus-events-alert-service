package com.ColumbusEventAlertService.services.Events;

import com.ColumbusEventAlertService.truncated.models.Event;
import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.services.events.NationwideEventService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import org.jsoup.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @InjectMocks
    private NationwideEventService subject;
    @Mock
    private JsoupService jsoupService;
    @Mock
    private Connection connection;
    @Mock
    private DateUtil dateUtil;
    private final String url = "Venue Url";

    @BeforeEach
    public void setUp() {
        subject = new NationwideEventService("test", url, jsoupService, dateUtil);
    }

    @Test
    public void testGetNextEvent_invalidUrl() {
        when(jsoupService.connect(anyString())).thenThrow(IllegalArgumentException.class);

        Event event = subject.getNextEvent();

        assertEquals("test", event.getLocationName());
        assertNull(event.getEventName());
        assertNull(event.getDate());
        assertNull(event.getTime());
    }
}
