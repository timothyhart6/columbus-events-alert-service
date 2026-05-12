package com.ColumbusEventAlertService.services.strategy.api;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbstractDiscoveryStrategyTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private TestDiscoveryStrategy strategy;

    private static final String VENUE_NAME = "Nationwide Arena";
    private static final String VENUE_ID = "KV-12345";
    private static final String API_KEY = "test-api-key";
    private static final String URL_TEMPLATE = "https://api.example.com/events?venueId={VENUE_ID}&apikey={API_KEY}";

    @BeforeEach
    void setUp() {
        strategy = new TestDiscoveryStrategy(VENUE_NAME, VENUE_ID, mockHttpClient);
        ReflectionTestUtils.setField(strategy, "apiKey", API_KEY);
        ReflectionTestUtils.setField(strategy, "url", URL_TEMPLATE);
    }

    @Test
    void getSourceName_returnsVenueName() {
        assertEquals(VENUE_NAME, strategy.getSourceName());
    }

    @Test
    void fetchCurrentDayEvents_returnsTodayEvents_whenApiRespondsWithTodayEvent() throws Exception {
        String today = LocalDate.now().toString();
        String json = """
                {
                  "_embedded": {
                    "events": [
                      {
                        "name": "Hockey Game",
                        "dates": { "start": { "localDate": "%s", "localTime": "19:00:00" } }
                      }
                    ]
                  }
                }
                """.formatted(today);

        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        List<Event> events = strategy.fetchCurrentDayEvents();

        assertEquals(1, events.size());
        assertEquals("Hockey Game", events.get(0).getName());
        assertEquals(VENUE_NAME, events.get(0).getLocationName());
        assertEquals(LocalDate.now(), events.get(0).getDate());
        assertEquals("7:00 PM", events.get(0).getTime());
    }

    @Test
    void fetchCurrentDayEvents_filtersOutNonTodayEvents_whenApiReturnsMixedDates() throws Exception {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String json = """
                {
                  "_embedded": {
                    "events": [
                      {
                        "name": "Today's Concert",
                        "dates": { "start": { "localDate": "%s", "localTime": "20:00:00" } }
                      },
                      {
                        "name": "Tomorrow's Concert",
                        "dates": { "start": { "localDate": "%s", "localTime": "20:00:00" } }
                      }
                    ]
                  }
                }
                """.formatted(today, tomorrow);

        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        List<Event> events = strategy.fetchCurrentDayEvents();

        assertEquals(1, events.size());
        assertEquals("Today's Concert", events.get(0).getName());
    }

    @Test
    void fetchCurrentDayEvents_returnsEmptyList_whenAllEventsAreInFuture() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String json = """
                {
                  "_embedded": {
                    "events": [
                      {
                        "name": "Future Event",
                        "dates": { "start": { "localDate": "%s", "localTime": "19:00:00" } }
                      }
                    ]
                  }
                }
                """.formatted(tomorrow);

        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        assertTrue(strategy.fetchCurrentDayEvents().isEmpty());
    }

    @Test
    void fetchCurrentDayEvents_returnsEmptyList_whenEmbeddedIsMissing() throws Exception {
        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{}");

        assertTrue(strategy.fetchCurrentDayEvents().isEmpty());
    }

    @Test
    void fetchCurrentDayEvents_formatsTimeAsAmPm_whenApiReturns24HourTime() throws Exception {
        String today = LocalDate.now().toString();
        String json = """
                {
                  "_embedded": {
                    "events": [
                      {
                        "name": "Afternoon Show",
                        "dates": { "start": { "localDate": "%s", "localTime": "13:30:00" } }
                      }
                    ]
                  }
                }
                """.formatted(today);

        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        List<Event> events = strategy.fetchCurrentDayEvents();

        assertEquals("1:30 PM", events.get(0).getTime());
    }

    @Test
    void fetchCurrentDayEvents_setsNullTime_whenApiDoesNotReturnTime() throws Exception {
        String today = LocalDate.now().toString();
        String json = """
                {
                  "_embedded": {
                    "events": [
                      {
                        "name": "No Time Event",
                        "dates": { "start": { "localDate": "%s" } }
                      }
                    ]
                  }
                }
                """.formatted(today);

        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        List<Event> events = strategy.fetchCurrentDayEvents();

        assertEquals(1, events.size());
        assertNull(events.get(0).getTime());
    }

    @Test
    void fetchCurrentDayEvents_throwsNetworkError_whenApiReturnsNon200Status() throws Exception {
        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(401);

        EventFetchException ex = assertThrows(EventFetchException.class,
                () -> strategy.fetchCurrentDayEvents());
        assertEquals(EventFetchException.ErrorType.NETWORK_ERROR, ex.getErrorType());
    }

    @Test
    void fetchCurrentDayEvents_throwsNetworkError_whenHttpClientThrows() throws Exception {
        when(mockHttpClient.<String>send(any(HttpRequest.class), any()))
                .thenThrow(new IOException("connection refused"));

        EventFetchException ex = assertThrows(EventFetchException.class,
                () -> strategy.fetchCurrentDayEvents());
        assertEquals(EventFetchException.ErrorType.NETWORK_ERROR, ex.getErrorType());
    }

    @Test
    void fetchCurrentDayEvents_throwsParsingError_whenResponseBodyIsMalformed() throws Exception {
        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("not valid json {{{");

        EventFetchException ex = assertThrows(EventFetchException.class,
                () -> strategy.fetchCurrentDayEvents());
        assertEquals(EventFetchException.ErrorType.PARSING_ERROR, ex.getErrorType());
    }

    static class TestDiscoveryStrategy extends AbstractDiscoveryStrategy {
        TestDiscoveryStrategy(String venueName, String venueId, HttpClient httpClient) {
            super(venueName, venueId, httpClient);
        }
    }
}
