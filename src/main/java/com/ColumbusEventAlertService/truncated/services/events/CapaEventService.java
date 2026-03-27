package com.ColumbusEventAlertService.truncated.services.events;

import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import org.jsoup.nodes.Document;

import java.time.LocalDate;

public class CapaEventService extends EventService {
    LocalDate today;

    public CapaEventService(JsoupService jsoupService, DateUtil dateUtil) {
        super(jsoupService, dateUtil);
    }

    @Override
    protected String getEventName(Document doc) {
        return doc.getElementsByClass("text-xl font-bold mt-0 mb-0").get(0).text();
    }

    @Override
    protected String getTime(Document doc) {
        return "";
    }

    @Override
    protected String getDateMonth(Document doc) {
        return dateUtil.getMonth();
    }

    @Override
    protected String getDateDay(Document doc) {
        return dateUtil.getDay();
    }
}
