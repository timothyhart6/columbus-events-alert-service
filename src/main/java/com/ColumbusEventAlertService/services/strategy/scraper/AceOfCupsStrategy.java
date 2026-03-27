package com.ColumbusEventAlertService.services.strategy.scraper;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import com.ColumbusEventAlertService.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class AceOfCupsStrategy extends AbstractWebScraperStrategy {

    public AceOfCupsStrategy(
            @Value("${venue-name.ace}") String sourceName,
            @Value("${url.ace}") String locationUrl) {
        super(sourceName, locationUrl);
    }

    // Each .event-title element corresponds to one event.
    // parseEvent navigates to its parent to find the date and time siblings.
    @Override
    protected Elements findAllElements(Document document) {
        return document.select(".seetickets-list-event-container");
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
                    .trafficCausing(false)
                    .interesting(true)
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
        Element nameElement = element.selectFirst(".event-title");
        if (nameElement == null || !nameElement.hasText()) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No event name found");
        }
        String name = nameElement.text().trim();
        if (name.isEmpty()) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Empty event name");
        }
        return name;
    }

    private LocalDate extractEventDate(Element element) throws EventFetchException {
        Element dateElement = element.selectFirst(".event-date");
        if (dateElement == null || !dateElement.hasText()) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No event date found");
        }

        // Date text format: "DayOfWeek MonthName DayNumber" e.g. "Sat March 25"
        String[] dateParts = dateElement.text().trim().split(" ");
        try {
            return DateUtil.parseMonthDayWithYear(dateParts[1] + " " + dateParts[2], "MMMM d");
        } catch (IllegalArgumentException e) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Failed to parse date: " + dateElement.text(),
                    e);
        }
    }

    private String extractEventTime(Element element) {
        if (element == null || !element.hasText()) return null;
        Element timeElement = element.selectFirst(".see-showtime");
        return timeElement != null ? timeElement.text().trim() : null;
    }
}
