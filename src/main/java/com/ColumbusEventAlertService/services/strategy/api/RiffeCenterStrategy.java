package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RiffeCenterStrategy extends AbstractDiscoveryStrategy {

    public RiffeCenterStrategy(
            @Value("${api.discovery.riffe-center.source-name}") String venueName,
            @Value("${api.discovery.riffe-center.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
