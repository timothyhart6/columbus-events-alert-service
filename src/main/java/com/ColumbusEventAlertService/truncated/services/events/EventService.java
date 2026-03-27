package com.ColumbusEventAlertService.truncated.services.events;

import com.ColumbusEventAlertService.truncated.models.Event;
import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public abstract class EventService {
    protected JsoupService jsoupService;
    protected DateUtil dateUtil;
    protected String venueName;
    protected String venueUrl;
    protected boolean isBadTraffic;
    protected boolean isDesiredEvent;


    @Autowired
    public EventService(JsoupService jsoupService, DateUtil dateUtil) {
        this.jsoupService = jsoupService;
        this.dateUtil = dateUtil;
    }

    public Event getNextEvent() throws IllegalArgumentException {
        Event event = new Event(venueName, isBadTraffic, isDesiredEvent);
        try {
            Document jsoupDocument = jsoupService.getDocument(jsoupService.connect(venueUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36"));
            setEventAttributes(jsoupDocument, event);
        } catch (IllegalArgumentException | IOException e) {
            log.info("Invalid URL: " + venueUrl);
        }
        logEventAttributes(event);
        return event;
    }

    private static void logEventAttributes(Event event) {
        log.info(
                event.getLocationName() + "\n" +
                event.getEventName() + "\n" +
                event.getDate() + "\n" +
                event.getTime() + "\n" +
                event.isBadTraffic() + "\n" +
                event.isDesiredEvent()
        );
    }

    public void setEventAttributes(Document doc, Event event) {
        String eventName;
        String year;
        String dateMonth;
        String dateDay;
        String time;

        try {
            eventName = getEventName(doc);
        } catch (IndexOutOfBoundsException e) {
            eventName = "Could not find event name";
            log.info("Event name node has changed location: " + e.getMessage());
        }
        try {
            year = getDateYear(doc);
        } catch (IndexOutOfBoundsException e) {
            year = "Could not find event year";
            log.info("Event year node has changed location: " + e.getMessage());
        }
        try {
            dateMonth = getDateMonth(doc);
        } catch (IndexOutOfBoundsException e) {
            dateMonth = "Could not find event month";
            log.info("Event month node has changed location: " + e.getMessage());
        }
        try {
            dateDay = getDateDay(doc);
        } catch (IndexOutOfBoundsException e) {
            dateDay = "Could not find event day";
            log.info("Event day node has changed location: " + e.getMessage());
        }
        try {
            time = getTime(doc);
        } catch (IndexOutOfBoundsException e) {
            time = "Could not find event time";
            log.info("Event time node has changed location: " + e.getMessage());
        }
        String formattedDate = dateMonth + "-" + dateDay + "-" + year;

        event.setEventName(eventName);
        event.setDate(formattedDate);
        event.setTime(time);
    }

    protected abstract String getEventName(Document doc);

    protected abstract String getTime(Document doc);

    protected abstract String getDateMonth(Document doc);

    protected abstract String getDateDay(Document doc);

    protected String getDateYear(Document doc) {
        return dateUtil.getYear();
    }

    String buildUrl(String template, LocalDate start, LocalDate end) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        return template
                .replace("{start-date}", start.format(formatter))
                .replace("{end-date}", end.format(formatter));
    }
}