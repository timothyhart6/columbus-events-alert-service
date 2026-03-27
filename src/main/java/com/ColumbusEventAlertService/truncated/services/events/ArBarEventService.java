package com.ColumbusEventAlertService.truncated.services.events;

import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ArBarEventService extends EventService {

    public ArBarEventService(@Value("${venue-name.ar-bar}") String venueName,
                             @Value("${url.ar-bar}") String venueUrl,
                             JsoupService jsoupService,
                             DateUtil dateUtil)
    {
        super(jsoupService, dateUtil);
        super.venueName = venueName;
        super.venueUrl = venueUrl;
        super.isBadTraffic = false;
        super.isDesiredEvent = true;
    }
    @Override
    protected String getEventName(Document doc) {
        return doc.getElementsByClass("info").get(0).select("h2").get(0).childNode(0).toString();
    }

    @Override
    protected String getDateMonth(Document doc) {
       String date = doc.getElementsByClass("date").get(0).childNode(3).childNode(1).childNode(0).toString();
       String[] monthAndDay = date.split(" ");
       String monthName = monthAndDay[0];
       return super.dateUtil.convertMonthNameToNumber(monthName);
    }

    @Override
    protected String getDateDay(Document doc) {
        String date = doc.getElementsByClass("date").get(0).childNode(3).childNode(1).childNode(0).toString();
        String[] monthAndDay = date.split(" ");
        return super.dateUtil.formatDay(monthAndDay[1]);
    }

    @Override
    protected String getTime(Document doc) {
        return doc.getElementsByClass("day-of-show-bar").get(0).getElementsByClass("doors-time").get(0).childNode(0).toString();
    }
}