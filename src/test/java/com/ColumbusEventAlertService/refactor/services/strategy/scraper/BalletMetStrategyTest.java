package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import com.ColumbusEventAlertService.refactor.models.Event;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BalletMetStrategyTest {

    private BalletMetStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BalletMetStrategy("Ballet MET", "https://test.com?start={start-date}&end={end-date}");
    }

    @Test
    void shouldParseEvent() throws Exception {
        String html = """
                <div class="production-container">
                    <h2>Swan Lake</h2>
                </div>
                """;

        Document doc = Jsoup.parse(html);
        Event event = strategy.parseEvent(doc.selectFirst(".production-container"));

        assertNotNull(event);
        assertEquals("Swan Lake", event.getName());
        assertEquals("Ballet MET", event.getLocationName());
        assertEquals(LocalDate.now(), event.getDate());
        assertNull(event.getTime());
        assertFalse(event.isTrafficCausing());
        assertTrue(event.isInteresting());
    }

    @Test
    void shouldReturnAllEvents() {
        String html = """
                <html>
                    <body>
                        <div class="production-container"><h2>Swan Lake</h2></div>
                        <div class="production-container"><h2>The Nutcracker</h2></div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        Elements elements = strategy.findAllElements(doc);

        assertEquals(2, elements.size());
    }

    @Test
    void shouldReturnEmptyWhenNoEvents() {
        String html = """
                <html>
                    <body>
                        <div>Some content</div>
                    </body>
                </html>
                """;

        Document doc = Jsoup.parse(html);
        assertTrue(strategy.findAllElements(doc).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenNameMissing() {
        String html = """
                <div class="production-container">
                </div>
                """;

        Document doc = Jsoup.parse(html);
        assertThrows(Exception.class, () -> strategy.parseEvent(doc.selectFirst(".production-container")));
    }
}
