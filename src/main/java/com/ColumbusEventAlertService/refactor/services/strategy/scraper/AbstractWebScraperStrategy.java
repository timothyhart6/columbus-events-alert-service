package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
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

@Slf4j
public abstract class AbstractWebScraperStrategy implements EventSourceStrategy {

    @Getter
    String locationName;
    String locationUrl;

    public AbstractWebScraperStrategy(String locationName, String locationUrl) {
        this.locationName = locationName;
        this.locationUrl = locationUrl;
    }

    public List<Event> fetchCurrentDayEvents()  {
        log.info("Fetching today's events for {}", getLocationName());

        try {
            Document document = fetchDocument();
            Elements eventElements = findAllElements(document);

            if (eventElements.isEmpty()) {
                log.info("No events found for {}", getLocationName());

                return Collections.emptyList();
            }

           ArrayList<Event> currentDayEvents = getCurrentDayEvents(eventElements);
            log.info("Found {} events today for {}", currentDayEvents.size(), getLocationName());

            return currentDayEvents;

            //TODO Add additional exceptions
        } catch (Exception e) {
            log.error("Error while fetching events for {}", getLocationName(), e);

            return Collections.emptyList();
        }
    }

     ArrayList<Event> getCurrentDayEvents(Elements eventElements) throws Exception {
        ArrayList<Event> currentDayEvents = new ArrayList<>();
        for (Element element : eventElements) {
            Event event = parseEvent(element);
            if (event != null && event.getDate().equals(LocalDate.now())) {
                currentDayEvents.add(event);
            }
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

    abstract Event parseEvent(Element element) throws Exception;

}
