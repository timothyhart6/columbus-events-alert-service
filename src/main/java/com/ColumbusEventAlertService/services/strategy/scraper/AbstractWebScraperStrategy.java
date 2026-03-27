package com.ColumbusEventAlertService.services.strategy.scraper;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import com.ColumbusEventAlertService.strategy.EventSourceStrategy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ColumbusEventAlertService.exception.EventFetchException.ErrorType.PARSING_ERROR;

@Slf4j
public abstract class AbstractWebScraperStrategy implements EventSourceStrategy {

    @Getter
    String sourceName;
    String locationUrl;

    public AbstractWebScraperStrategy(String sourceName, String locationUrl) {
        this.sourceName = sourceName;
        this.locationUrl = locationUrl;
    }

    public List<Event> fetchCurrentDayEvents() throws EventFetchException {
        log.info("Fetching today's events for {}", getSourceName());

        try {
            Document document = fetchDocument();
            Elements eventElements = findAllElements(document);

            if (eventElements.isEmpty()) {
                log.info("No events found for {}", getSourceName());

                return Collections.emptyList();
            }
            ArrayList<Event> currentDayEvents = getCurrentDayEvents(eventElements);
            log.info("Found {} events today for {}", currentDayEvents.size(), getSourceName());

            return currentDayEvents;
        } catch (IOException e) {
            throw new EventFetchException(
                    sourceName,
                    EventFetchException.ErrorType.NETWORK_ERROR,
                    "Failed to connect to venue website: " + sourceName,
                    e
            );
        } catch (Exception e) {
            throw new EventFetchException(
                    sourceName,
                    EventFetchException.ErrorType.UNKNOWN_ERROR,
                    "Unknown failure occurred when attempting to get events for: " + sourceName,
                    e
            );
        }
    }

     ArrayList<Event> getCurrentDayEvents(Elements eventElements) throws EventFetchException {
        ArrayList<Event> currentDayEvents = new ArrayList<>();
        int elementCounter = 0;
         LocalDate today = LocalDate.now();
         for (Element element : eventElements) {

            try {
                Event event = parseEvent(element);
                if (event != null && event.getDate().equals(today)) {
                    currentDayEvents.add(event);
                }
            } catch (EventFetchException e) {
               if (e.getErrorType().equals(PARSING_ERROR)) {
                   //continue trying to parse other events
                   log.error("{} thrown while parsing event #{} for {}. Full exception message: {}", e.getErrorType(), elementCounter, e.getSourceName(), e.getMessage());
               } else {
                   throw e;
               }
           }

             elementCounter++;
        }
        return currentDayEvents;
    }

     Document fetchDocument() throws IOException {
        return Jsoup.connect(locationUrl).get();
    }

    protected String extractText(Element parent, String cssSelector) {
        Element element = parent.selectFirst(cssSelector);
        return element != null ? element.text().trim() : null;
    }

    abstract Elements findAllElements(Document document);

    abstract Event parseEvent(Element element) throws EventFetchException;

}
