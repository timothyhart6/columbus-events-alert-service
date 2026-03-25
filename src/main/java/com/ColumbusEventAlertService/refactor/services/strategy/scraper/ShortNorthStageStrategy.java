package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShortNorthStageStrategy extends AbstractCapaStrategy {

    public ShortNorthStageStrategy(
            @Value("${venue-name.sn-stage}") String sourceName,
            @Value("${url.sn-stage}") String locationUrlTemplate) {
        super(sourceName, locationUrlTemplate, true, false);
    }
}
