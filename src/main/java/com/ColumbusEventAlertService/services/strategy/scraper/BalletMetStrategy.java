package com.ColumbusEventAlertService.services.strategy.scraper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BalletMetStrategy extends AbstractCapaStrategy {

    public BalletMetStrategy(
            @Value("${venue-name.ballet-met}") String sourceName,
            @Value("${url.ballet-met}") String locationUrlTemplate) {
        super(sourceName, locationUrlTemplate, false, true);
    }
}
