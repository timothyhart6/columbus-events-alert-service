package com.ColumbusEventAlertService.services.strategy.scraper;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
//@Component
public class NationwideStrategy extends AbstractWebScraperStrategy {

    public NationwideStrategy(
            @Value("${venue-name.nationwide}") String sourceName,
            @Value("${url.nationwide}") String locationUrl) {
        super(sourceName, locationUrl);
    }

    @Override
    Document fetchDocument() throws IOException {
        return Jsoup.connect(locationUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get();
    }

    // TODO: verify this container selector against Nationwide Arena's actual HTML
    @Override
    protected Elements findAllElements(Document document) {
        Elements events = new Elements();
        try {
            events = document.select(".eventItem");
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
            String eventName = extractEventName(element);
            LocalDate eventDate = extractEventDate(element);
            String eventTime = extractEventTime(element);

            return Event.builder()
                    .locationName(getSourceName())
                    .name(eventName)
                    .date(eventDate)
                    .time(eventTime)
                    .trafficCausing(true)
                    .interesting(false)
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

    private String extractEventName(Element element) throws EventFetchException {
        Element titleElement = element.selectFirst(".title");
        if (titleElement == null) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No event name found");
        }
        String name = titleElement.text().trim();
        if (name.isEmpty()) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Empty event name");
        }
        return name;
    }

    private LocalDate extractEventDate(Element element) throws EventFetchException {
        Element monthEl = element.selectFirst(".m-date__month");
        Element dayEl = element.selectFirst(".m-date__day");
        Element yearEl = element.selectFirst(".m-date__year");

        if (monthEl == null || dayEl == null || yearEl == null) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Missing date elements");
        }

        String monthName = monthEl.text().trim();
        String day = dayEl.text().replaceAll("\\D", "").trim();
        String year = yearEl.text().replaceAll("\\D", "").trim();

        try {
            return LocalDate.parse(monthName + " " + day + " " + year,
                    DateTimeFormatter.ofPattern("MMMM d yyyy"));
        } catch (Exception e) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Failed to parse date: " + monthName + " " + day + " " + year,
                    e);
        }
    }

    private String extractEventTime(Element element) {
        Element timeEl = element.selectFirst(".time .start");
        return timeEl != null ? timeEl.text().trim() : null;
    }
}
