package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import com.ColumbusEventAlertService.refactor.models.Event;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class AceOfCupsStrategyTest {

    private AceOfCupsStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AceOfCupsStrategy("Ace Of Cups", "https://test.com");
    }

    @Test
    void shouldParseCompleteEvent() throws Exception {
        String html = """
                <div class="seetickets-list-event-container">
                    <div class="event-title">Jazz Night</div>
                    <div class="event-date">Sat March 15</div>
                    <div class="see-showtime">8:00 PM</div>
                </div>
                """;

        Document doc = Jsoup.parse(html);
        Event event = strategy.parseEvent(doc.selectFirst(".seetickets-list-event-container"));
        int thisYear = Year.now().getValue();

        assertNotNull(event);
        assertEquals("Jazz Night", event.getName());
        assertEquals("Ace Of Cups", event.getLocationName());
        assertThat(event.getDate().getYear(), anyOf(is(thisYear), is(thisYear + 1)));
        assertEquals(3, event.getDate().getMonth().getValue());
        assertEquals(15, event.getDate().getDayOfMonth());
        assertEquals("8:00 PM", event.getTime());
        assertTrue(event.isInteresting());
        assertFalse(event.isTrafficCausing());
    }

    @Test
    void shouldHandleMissingTime() throws Exception {
        String html = """
                <div class="seetickets-list-event-container">
                    <div class="event-title">Jazz Night</div>
                    <div class="event-date">Sat March 15</div>
                </div>
                """;

        Document doc = Jsoup.parse(html);
        Event event = strategy.parseEvent(doc.selectFirst(".seetickets-list-event-container"));

        assertNotNull(event);
        assertEquals("Jazz Night", event.getName());
        assertNull(event.getTime());
    }

    @Test
    void shouldTrimWhitespace() throws Exception {
        String html = """
                <div class="seetickets-list-event-container">
                    <div class="event-title">  The Beths  </div>
                    <div class="event-date">  Fri April 20  </div>
                    <div class="see-showtime">  7:30 PM  </div>
                </div>
                """;

        Document doc = Jsoup.parse(html);
        Event event = strategy.parseEvent(doc.selectFirst(".seetickets-list-event-container"));

        assertEquals("The Beths", event.getName());
        assertEquals(4, event.getDate().getMonth().getValue());
        assertEquals(20, event.getDate().getDayOfMonth());
        assertEquals("7:30 PM", event.getTime());
    }

    @Test
    void shouldThrowExceptionWhenNameMissing() {
        String html = """
                <div class="seetickets-list-event-container">
                    <div class="event-date">Sat March 15</div>
                    <div class="see-showtime">8:00 PM</div>
                </div>
                """;

        Document doc = Jsoup.parse(html);
        assertThrows(Exception.class, () -> strategy.parseEvent(doc.selectFirst(".seetickets-list-event-container")));
    }

    @Test
    void shouldThrowExceptionWhenDateMissing() {
        String html = """
                <div class="seetickets-list-event-container">
                    <div class="event-title">Jazz Night</div>
                    <div class="see-showtime">8:00 PM</div>
                </div>
                """;

        Document doc = Jsoup.parse(html);
        assertThrows(Exception.class, () -> strategy.parseEvent(doc.selectFirst(".seetickets-list-event-container")));
    }

    @Test
    void shouldThrowExceptionWhenDateFormatInvalid() {
        String html = """
                <div class="seetickets-list-event-container">
                    <div class="event-title">Jazz Night</div>
                    <div class="event-date">InvalidDate</div>
                </div>
                """;

        Document doc = Jsoup.parse(html);
        assertThrows(Exception.class, () -> strategy.parseEvent(doc.selectFirst(".seetickets-list-event-container")));
    }

    @Test
    void shouldReturnAllEventContainers() {
        String html = """
                <html>
                    <body>
                        <div class="seetickets-list-event-container">
                            <div class="event-title">Jazz Night</div>
                            <div class="event-date">Sat March 15</div>
                        </div>
                        <div class="seetickets-list-event-container">
                            <div class="event-title">Trivia Night</div>
                            <div class="event-date">Sun March 16</div>
                        </div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        Elements elements = strategy.findAllElements(doc);

        assertEquals(2, elements.size());
    }

    @Test
    void shouldReturnEmptyWhenNoEventContainers() {
        String html = """
                <html>
                    <body>
                        <div>Some content</div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        Elements elements = strategy.findAllElements(doc);

        assertTrue(elements.isEmpty());
    }
}
