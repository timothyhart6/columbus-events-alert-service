package com.ColumbusEventAlertService.truncated.services.events;

import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NationwideEventService extends EventService {

    public NationwideEventService(@Value("${venue-name.nationwide}") String venueName,
                                  @Value("${url.nationwide}") String venueUrl,
                                  JsoupService jsoupService,
                                  DateUtil dateUtil)
    {
        super(jsoupService, dateUtil);
        super.venueName = venueName;
        super.venueUrl = venueUrl;
        super.isBadTraffic = true;
        super.isDesiredEvent = false;
    }

    @Override
    protected String getEventName(Document doc) {
       return doc.getElementsByClass("title title-withTagline ").get(0).childNode(1).childNode(0).toString().trim();
    }

    @Override
    protected String getTime(Document doc) {
        return doc.getElementsByClass("time").get(0).getElementsByClass("start").get(0).childNode(0).toString();
    }
    @Override
    protected String getDateYear(Document doc) {
        Node yearNode = doc.getElementsByClass("m-date__year").get(0).childNode(0);
        return yearNode.toString().replaceAll("\\D", "");
    }
    @Override
    protected String getDateMonth(Document doc) {
        Node monthNode = doc.getElementsByClass("m-date__month").get(0).childNode(0);
        String dateMonth = monthNode.toString().trim();
        dateMonth = super.dateUtil.convertMonthNameToNumber(dateMonth);
        return dateMonth;
    }
    @Override
    protected String getDateDay(Document doc) {
        Node dayNode = doc.getElementsByClass("m-date__day").get(0).childNode(0);
        String dateDay = dayNode.toString().replaceAll("\\D", "");
        dateDay = super.dateUtil.formatDay(dateDay);
        return dateDay;
    }
}