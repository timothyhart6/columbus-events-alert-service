package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import com.ColumbusEventAlertService.refactor.exception.EventFetchException;
import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

@Slf4j
public class KembaLiveStrategy extends AbstractWebScraperStrategy {

    public KembaLiveStrategy(
            @Value("${venue-name.kemba}") String locationName,
            @Value("${url.kemba}") String locationUrl) {
        super(locationName, locationUrl);
    }

    protected Event parseEvent(Element element) throws EventFetchException {
       try {
           String eventName = extractEventName(element.select("h2").get(0));
           Elements dateTimeElements = extractDateTimeElements(element);
           String dateText = dateTimeElements.get(0).text().trim();
           LocalDate eventDate = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

           String eventTime = null;
           if (dateTimeElements.size() > 1) {
               eventTime = dateTimeElements.get(1).text().trim();
           }

           return Event.builder()
                   .locationName(getLocationName())
                   .name(eventName)
                   .date(eventDate)
                   .time(eventTime)
                   .causesTraffic(false)
                   .interesting(true)
                   .build();

       }  catch (IllegalArgumentException e) {
           throw new EventFetchException(
                   getLocationName(),
                   EventFetchException.ErrorType.PARSING_ERROR,
                   "Failed to parse date",
                   e
           );
       } catch (Exception e) {
           throw new EventFetchException(
                   getLocationName(),
                   EventFetchException.ErrorType.PARSING_ERROR,
                   "Unexpected error during parsing",
                   e
           );
       }
   }

    protected Elements findAllElements(Document document) {
        Elements events = new Elements();
        try {
            events = document.select(".events-list").get(0).children();
        } catch (IndexOutOfBoundsException e) {
            log.error("No events found for {}", getLocationName());
        }
        return events;
    }

    protected String extractEventName(Element element) throws EventFetchException {
        String eventName = extractText(element, "h2");
        if(eventName == null || eventName.isEmpty()) {
            log.error("No event name found for {}", getLocationName());
            throw new EventFetchException(
                    getLocationName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No event name found");
        }
        return eventName;
    }

    private Elements extractDateTimeElements(Element element) throws EventFetchException {
        Elements dateTimeElements = element.select(".doors-time");

        if(dateTimeElements.isEmpty()) {
            throw new EventFetchException(
                    getLocationName(),
                    EventFetchException.ErrorType.PARSING_ERROR,
                    "No date/time element found");
        }

        return dateTimeElements;
    }

}
