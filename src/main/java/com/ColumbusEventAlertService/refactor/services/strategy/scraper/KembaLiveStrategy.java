package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import com.ColumbusEventAlertService.refactor.models.Event;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeFormatter;

@Slf4j
public class KembaLiveStrategy extends AbstractWebScraperStrategy {

    public KembaLiveStrategy(
            @Value("${venue-name.kemba}") String locationName,
            @Value("${url.kemba}") String locationUrl) {
        super(locationName, locationUrl);
    }

    protected Event parseEvent(Element element) throws Exception {
        String eventName = extractEventName(element.select("h2").get(0));
        Elements dateTimeElements = element.select(".doors-time");

        if(dateTimeElements.isEmpty()) {
            log.error("No date/time found for {}", getLocationName());
            //todo make specific exception
            throw new Exception();
        }

        String dateText = dateTimeElements.get(0).text().trim();
        LocalDate eventDate = extractEventDate(dateText);

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
    }

    protected Elements findAllElements(Document document) {
        return document.select(".events-list").get(0).children();
    }

    protected String extractEventName(Element element) throws Exception {
        String eventName = extractText(element, "h2");
        if(eventName == null || eventName.isEmpty()) {
            log.error("No event name found for {}", getLocationName());
            //todo use more specific exception
            throw new Exception("No event name");
        }
        return eventName;
    }


    protected LocalDate extractEventDate(String dateText) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d");
        MonthDay monthDay = MonthDay.parse(dateText, formatter);;
        int year = Year.now().getValue();
        LocalDate date = monthDay.atYear(year);

        if(date.isBefore(LocalDate.now())) {
           date = date.plusYears(1);
        }

        return date;
    }


}
