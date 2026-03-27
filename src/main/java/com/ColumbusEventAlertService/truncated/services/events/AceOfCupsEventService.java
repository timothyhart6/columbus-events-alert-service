package com.ColumbusEventAlertService.truncated.services.events;

import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AceOfCupsEventService extends EventService{

    public AceOfCupsEventService(
            @Value("${venue-name.ace}") String venueName,
            @Value("${url.ace}") String venueUrl,
            JsoupService jsoupService,
            DateUtil dateUtil) {
        super(jsoupService, dateUtil);
        super.venueName = venueName;
        super.venueUrl = venueUrl;
        super.isBadTraffic = false;
        super.isDesiredEvent = true;
    }

    @Override
    protected String getEventName(Document doc) {
        return doc.getElementsByClass("event-title").get(0).firstChild().firstChild().toString().trim();
    }

    @Override
    protected String getTime(Document doc) {
        return doc.getElementsByClass("see-showtime ").get(0).childNode(0).toString().trim();
    }

    @Override
    protected String getDateMonth(Document doc) {
        String date = doc.getElementsByClass("event-date").get(0).childNode(0).toString().trim();
        String[] monthAndDay = date.split(" ");
        String monthName = monthAndDay[1];
        return super.dateUtil.convertMonthNameToNumber(monthName);
    }

    @Override
    protected String getDateDay(Document doc) {
        String date = doc.getElementsByClass("event-date").get(0).childNode(0).toString().trim();
        String[] monthAndDay = date.split(" ");
        return super.dateUtil.formatDay(monthAndDay[2]);
    }
}