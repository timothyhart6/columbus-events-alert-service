package com.ColumbusEventAlertService.truncated.services.events;

import com.ColumbusEventAlertService.truncated.services.JsoupService;
import com.ColumbusEventAlertService.truncated.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShortNorthStageService extends CapaEventService {

    public ShortNorthStageService(
            @Value("${venue-name.sn-stage}") String venueName,
            @Value("${url.sn-stage}") String venueUrlTemplate,
            JsoupService jsoupService,
            DateUtil dateUtil) {
        super(jsoupService, dateUtil);
        this.today = dateUtil.getCurrentDate();
        super.venueName = venueName;
        super.venueUrl = buildUrl(venueUrlTemplate, today, today);
        super.isBadTraffic = true;
        super.isDesiredEvent = false;
    }

}
