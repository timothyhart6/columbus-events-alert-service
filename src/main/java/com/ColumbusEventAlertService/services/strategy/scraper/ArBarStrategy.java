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
public class ArBarStrategy extends AbstractWebScraperStrategy {

    public ArBarStrategy(
            @Value("${venue-name.ar-bar}") String sourceName,
            @Value("${url.ar-bar}") String locationUrl) {
        super(sourceName, locationUrl);
    }

    @Override
    protected Elements findAllElements(Document document) {
        Elements events = new Elements();
        try {
            events = document.select(".events-list").get(0).children();
        } catch (IndexOutOfBoundsException e) {
            log.error("No events found for {}", getSourceName());
        }
        return events;
    }

    @Override
    protected Event parseEvent(Element element) throws EventFetchException {
        try {
            String eventName = extractEventName(element);
            Elements dateTimeElements = extractDateTimeElements(element);
            String dateText = dateTimeElements.get(0).text().trim();
            LocalDate eventDate = extractEventDate(dateText);

            String eventTime = null;
            Elements timeElements = element.select(".day-of-show-bar .doors-time");
            if (!timeElements.isEmpty()) {
                eventTime = timeElements.get(0).text().trim();
            }

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
        String eventName = extractText(element, "h2");
        if (eventName == null || eventName.isEmpty()) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No event name found");
        }
        return eventName;
    }

    private Elements extractDateTimeElements(Element element) throws EventFetchException {
        Elements dateTimeElements = element.select(".doors-time");
        if (dateTimeElements.isEmpty()) {
            throw new EventFetchException(
                    getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No date/time element found");
        }
        return dateTimeElements;
    }

    private LocalDate extractEventDate(String dateText) throws EventFetchException {
        if (dateText == null || dateText.isEmpty()) {
            throw new EventFetchException(getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No date text found");
        }
        try {
            return DateUtil.parseMonthDayWithYear(dateText, "MMMM d");
        } catch (IllegalArgumentException e) {
            throw new EventFetchException(getSourceName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "Failed to parse date: " + dateText,
                    e);
        }
    }
}
