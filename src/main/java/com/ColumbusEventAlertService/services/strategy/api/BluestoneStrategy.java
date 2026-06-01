package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BluestoneStrategy extends AbstractDiscoveryStrategy {

    public BluestoneStrategy(
            @Value("${api.discovery.bluestone.source-name}") String venueName,
            @Value("${api.discovery.bluestone.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
