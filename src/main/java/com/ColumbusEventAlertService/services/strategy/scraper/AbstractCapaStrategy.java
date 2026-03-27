package com.ColumbusEventAlertService.services.strategy.scraper;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public abstract class AbstractCapaStrategy extends AbstractWebScraperStrategy {

    private final boolean trafficCausing;
    private final boolean interesting;

    protected AbstractCapaStrategy(String sourceName, String locationUrlTemplate, boolean trafficCausing, boolean interesting) {
        super(sourceName, buildTodayUrl(locationUrlTemplate));
        this.trafficCausing = trafficCausing;
        this.interesting = interesting;
    }

    private static String buildTodayUrl(String template) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return template
                .replace("{start-date}", today)
                .replace("{end-date}", today);
    }

    @Override
    protected Elements findAllElements(Document document) {
        Elements events = new Elements();
        try {
            events = document.select(".production-container");
            if (events.isEmpty()) {
                log.info("No events found for {}", getSourceName());
            }
        } catch (Exception e) {
            log.error("Failed to find event elements for {}", getSourceName());
        }
        return events;
    }

    @Override
    protected Event parseEvent(Element element) throws EventFetchException {
        try {
            String eventName = extractText(element, "h2");
            if (eventName == null || eventName.isEmpty()) {
                throw new EventFetchException(
                        getSourceName(),
                        EventFetchException.ErrorType.PARSING_ERROR,
                        "No event name found");
            }

            return Event.builder()
                    .locationName(getSourceName())
                    .name(eventName)
                    .date(LocalDate.now())
                    .time(null)
                    .trafficCausing(trafficCausing)
                    .interesting(interesting)
                    .build();
        } catch (EventFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Unexpected error during parsing",
                    e
            );
        }
    }
}
