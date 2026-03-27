package com.ColumbusEventAlertService.services.Events;

import com.ColumbusEventAlertService.truncated.models.Event;
import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.services.events.AceOfCupsEventService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AceOfCupsEventServiceTest {

    @Mock
    private DateUtil dateUtil;
    @Mock
    private JsoupService jsoupService;
    @Mock
    private Connection connection;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Document document;

    @Test
    public void testGetUpcomingEvent_success() throws Exception {
        AceOfCupsEventService subject = new AceOfCupsEventService("venue", "url", jsoupService, dateUtil);

        when(connection.userAgent(anyString())).thenReturn(connection);
        when(jsoupService.connect(anyString())).thenReturn(connection);
        when(jsoupService.getDocument(connection)).thenReturn(document);

        //DeepStub all the fields
        when(jsoupService.getDocument(connection.userAgent(anyString())).getElementsByClass("event-title").get(0).firstChild().firstChild().toString().trim()).thenReturn("Ace of Cups Event");
        when(jsoupService.getDocument(connection.userAgent(anyString())).getElementsByClass("event-date").get(0).childNode(0).toString().trim()).thenReturn("Tue September 09");
        when(dateUtil.convertMonthNameToNumber(anyString())).thenReturn("09");
        when(dateUtil.formatDay(anyString())).thenReturn("09");
        when(jsoupService.getDocument(connection.userAgent(anyString())).getElementsByClass("see-showtime ").get(0).childNode(0).toString().trim()).thenReturn("8:00 PM");

        Event event = subject.getNextEvent();

        assertEquals("Ace of Cups Event", event.getEventName());
        assertTrue(event.getDate().startsWith("09-09"));
        assertEquals("8:00 PM", event.getTime());
    }
}