package com.ColumbusEventAlertService.services.Events;

import com.ColumbusEventAlertService.truncated.models.Event;
import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.services.events.ShortNorthStageService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CapaEventServiceTest {
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
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(jsoupService.connect(anyString())).thenReturn(connection);
        when(jsoupService.getDocument(connection)).thenReturn(document);

        //DeepStub all the fields
        when(jsoupService.getDocument(connection.userAgent(anyString())).getElementsByClass("text-xl font-bold mt-0 mb-0").get(0).text()).thenReturn("Capa Event");
        when(dateUtil.getCurrentDate()).thenCallRealMethod();

        ShortNorthStageService subject = new ShortNorthStageService("Short North Stage", "Url", jsoupService, dateUtil);
        Event event = subject.getNextEvent();

        assertEquals("Capa Event", event.getEventName());
        assertEquals("", event.getTime());
    }
}
