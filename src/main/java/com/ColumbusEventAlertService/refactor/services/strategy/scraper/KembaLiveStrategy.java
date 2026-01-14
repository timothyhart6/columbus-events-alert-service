package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import com.ColumbusEventAlertService.refactor.models.Event;
import com.ColumbusEventAlertService.refactor.strategy.EventSourceStrategy;
import com.ColumbusEventAlertService.services.JsoupService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class KembaLiveStrategy implements EventSourceStrategy {
    JsoupService jsoupService;
    String url;

    public KembaLiveStrategy(JsoupService jsoupService, String url) {
        this.jsoupService = jsoupService;
        this.url = url;
    }

    @Override
    public List<Event> fetchTodaysEvents() throws Exception {
        log.info("Fetching todays events for {}", getLocationName());

        try {
            ArrayList<Event> todaysEvents = new ArrayList<>();
            Document document = fetchDocument();
            Elements eventElements = findAllElements(document);

            if (eventElements.isEmpty()) {
                log.info("No events found for {}", getLocationName());
                return Collections.emptyList();
            }

            for (Element element : eventElements) {
                Event event = parseEvent(element);
                if (event != null && event.getDate().equals(LocalDate.now())) {
                    todaysEvents.add(event);
                }
            }
            log.info("Found {} events today for {}", todaysEvents.size(), getLocationName());

            return todaysEvents;

        } catch ( Exception e ) {
            log.error("Error while fetching events for {}", getLocationName(), e);
        }

        return Collections.emptyList();
    }

    private Event parseEvent(Element element) throws Exception {
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

    private Elements findAllElements(Document document) {
        Elements events = document.select(".events-list").get(0).children();
        return events;
    }

    private Document fetchDocument() throws IOException {
        return Jsoup.connect(url).get();
    }

    private String extractEventName(Element element) throws Exception {
        String eventName = extractText(element, "h2");
        if(eventName == null || eventName.isEmpty()) {
            log.error("No event name found for {}", getLocationName());
            //todo use more specific exception
            throw new Exception("No event name");
        }
        return eventName;
    }


    private LocalDate extractEventDate(String dateText) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d");
        MonthDay monthDay = MonthDay.parse(dateText, formatter);;
        int year = Year.now().getValue();
        LocalDate date = monthDay.atYear(year);

        if(date.isBefore(LocalDate.now())) {
           date = date.plusYears(1);
        }

        return date;
    }

    private String extractText(Element parent, String cssSelector) throws Exception {
        Element element = parent.selectFirst(cssSelector);
        return element != null ? element.text().trim() : null;
    }

    @Override
    public String getLocationName() {
        return "Kemba Live";
    }

}
