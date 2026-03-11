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

public class KembaLiveStrategyTest {
    private KembaLiveStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new KembaLiveStrategy("Kemba Live!", "https://test.com");
    }

    @Test
    void shouldParseCompleteEvent() throws Exception {
        String html = """
            <div class="event-item">
                <h2>Snow Strippers</h2>
                <span class="doors-time">March 15</span>
                <span class="doors-time">8:00 PM</span>
            </div>
            """;

        Document doc = Jsoup.parse(html);
        Event event = strategy.parseEvent(doc.selectFirst(".event-item"));
        int thisYear = Year.now().getValue();

        assertNotNull(event);
        assertEquals("Snow Strippers", event.getName());
        assertEquals("Kemba Live!", event.getLocationName());
        assertNotNull(event.getDate());
        assertThat(event.getDate().getYear(), anyOf(is(thisYear), is(thisYear + 1)));
        assertEquals(3, event.getDate().getMonth().getValue() );
        assertEquals(15, event.getDate().getDayOfMonth());
        assertEquals("8:00 PM", event.getTime().orElse(null));
        assertTrue(event.isInteresting());
        assertFalse(event.isTrafficCausing());

    }

    @Test
    void shouldHandleMissingTime() throws Exception {
        String html = """
            <div class="event-item">
                <h2>Jazz Night</h2>
                <span class="doors-time">April 20</span>
            </div>
            """;

        Document doc = Jsoup.parse(html);
        Event event = strategy.parseEvent(doc.selectFirst(".event-item"));

        assertNotNull(event);
        assertEquals("Jazz Night", event.getName());
        assertEquals("Kemba Live!", event.getLocationName());
        assertEquals(4, event.getDate().getMonth().getValue() );
        assertEquals(20, event.getDate().getDayOfMonth());
        assertNull(event.getTime().orElse(null), "Time should be null when not provided");
    }

    @Test
    void shouldTrimWhitespace() throws Exception {
        String html = """
            <div class="event-item">
                <h2>  The Lumineers  </h2>
                <span class="doors-time">  May 10  </span>
                <span class="doors-time">  7:30 PM  </span>
            </div>
            """;

        Document doc = Jsoup.parse(html);
        Event event = strategy.parseEvent(doc.selectFirst(".event-item"));

        assertEquals("The Lumineers", event.getName());
        assertEquals(5, event.getDate().getMonth().getValue() );
        assertEquals(10, event.getDate().getDayOfMonth());
        assertEquals("7:30 PM", event.getTime().orElse(null));
    }

    @Test
    void shouldThrowExceptionWhenNameMissing() {
        String html = """
            <div class="event-item">
                <span class="doors-time">January 20</span>
            </div>
            """;

        Document doc = Jsoup.parse(html);

        assertThrows(Exception.class, () -> strategy.parseEvent(doc.selectFirst(".event-item")));

    }

    @Test
    void shouldThrowExceptionWhenDateTimeMissing() {
        String html = """
            <div class="event-item">
                <h2>Some Event</h2>
            </div>
            """;

        Document doc = Jsoup.parse(html);

        assertThrows(Exception.class, () -> strategy.parseEvent(doc.selectFirst(".event-item")));
    }

    @Test
    void shouldThrowExceptionWhenDateFormatInvalid() {
        String html = """
            <div class="event-item">
                <h2>Some Event</h2>
                <span class="doors-time">InvalidDate</span>
            </div>
            """;

        Document doc = Jsoup.parse(html);

        assertThrows(Exception.class, () -> strategy.parseEvent(doc.selectFirst(".event-item")));
    }

    @Test
    void shouldReturnEmptyListWhenElementsMissing() {
        String html = """
            <html>
                <body>
                    <div>Some content</div>
                </body>
            </html>
            """;

        Document doc = Jsoup.parse(html);
        Elements elements = strategy.findAllElements(doc);

        assertEquals(0, elements.size());
    }

    @Test
    void shouldReturnEmptyWhenElementsAreMissing() {
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

    @Test
    void shouldReturnEmptyWhenNoEvents() {
        String html = """
            <html>
                <body>
                    <div class="events-list">
                    </div>
                </body>
            </html>
            """;

        Document doc = Jsoup.parse(html);
        Elements elements = strategy.findAllElements(doc);

        assertTrue(elements.isEmpty());
    }

    @Test
    void shouldParseCompleteHtmlPage() throws Exception {
        String html = """
                <html>
                    <head><title>Kemba Live Events</title></head>
                    <body>
                        <div class="events-list">
                            <div class="event-item">
                                <div>
                                    <h2>The Lumineers</h2>
                                </div>
                                <span class="doors-time">March 15</span>
                                <span class="doors-time">7:30 PM</span>
                            </div>
                            <div class="event-item">
                                <div>
                                    <h2>Imagine Dragons</h2>
                                </div>
                                <span class="doors-time">March 20</span>
                                <span class="doors-time">8:00 PM</span>
                            </div>
                            <div class="event-item">
                                <div>
                                    <h2>Twenty One Pilots</h2>
                                </div>
                                <span class="doors-time">April 5</span>
                            </div>
                        </div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        Elements eventElements = strategy.findAllElements(doc);

        assertEquals(3, eventElements.size());

        // Parse first event (with time)
        Event event1 = strategy.parseEvent(eventElements.get(0));
        assertEquals("The Lumineers", event1.getName());
        assertEquals("7:30 PM", event1.getTime().orElse(null));
        assertEquals("Kemba Live!", event1.getLocationName());

        // Parse second event (with time)
        Event event2 = strategy.parseEvent(eventElements.get(1));
        assertEquals("Imagine Dragons", event2.getName());
        assertEquals("8:00 PM", event2.getTime().orElse(null));

        // Parse third event (no time)
        Event event3 = strategy.parseEvent(eventElements.get(2));
        assertEquals("Twenty One Pilots", event3.getName());
        assertNull(event3.getTime().orElse(null));
    }
    @Test
    void shouldHandleMixedEvents() {
        String html = """
                <html>
                    <body>
                        <div class="events-list">
                            <div class="event-item">
                                <h2>Valid Event</h2>
                                <span class="doors-time">March 15</span>
                            </div>
                            <div class="event-item">
                                <!-- Invalid: missing h2 -->
                                <span class="doors-time">March 20</span>
                            </div>
                            <div class="event-item">
                                <h2>Another Valid Event</h2>
                                <span class="doors-time">March 25</span>
                            </div>
                        </div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        Elements eventElements = strategy.findAllElements(doc);

        assertEquals(3, eventElements.size());

        // First event should parse successfully
        assertDoesNotThrow(() -> {
            Event event1 = strategy.parseEvent(eventElements.get(0));
            assertEquals("Valid Event", event1.getName());
        });

        // Second event should throw exception
        assertThrows(Exception.class, () -> strategy.parseEvent(eventElements.get(1)));

        // Third event should parse successfully
        assertDoesNotThrow(() -> {
            Event event3 = strategy.parseEvent(eventElements.get(2));
            assertEquals("Another Valid Event", event3.getName());
        });
    }
}