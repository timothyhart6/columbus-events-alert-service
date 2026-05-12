package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HuntingtonParkStrategy extends AbstractDiscoveryStrategy {

    public HuntingtonParkStrategy(
            @Value("${api.discovery.huntington.source-name}") String venueName,
            @Value("${api.discovery.huntington.venue-id}") String venueId) {
        super(venueName, venueId, true, true);
    }

}
