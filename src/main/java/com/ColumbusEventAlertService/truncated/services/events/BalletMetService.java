package com.ColumbusEventAlertService.truncated.services.events;

import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BalletMetService extends CapaEventService {

    public BalletMetService(
            @Value("${venue-name.ballet-met}") String venueName,
            @Value("${url.ballet-met}") String venueUrlTemplate,
            JsoupService jsoupService,
            DateUtil dateUtil) {
        super(jsoupService, dateUtil);
        this.today = dateUtil.getCurrentDate();
        super.venueName = venueName;
        super.venueUrl = buildUrl(venueUrlTemplate, today, today);
        super.isBadTraffic = false;
        super.isDesiredEvent = true;
    }

}
