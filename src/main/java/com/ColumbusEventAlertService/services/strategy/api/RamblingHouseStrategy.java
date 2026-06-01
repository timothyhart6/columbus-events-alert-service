package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RamblingHouseStrategy extends AbstractDiscoveryStrategy {

    public RamblingHouseStrategy(
            @Value("${api.discovery.rambling-house.source-name}") String venueName,
            @Value("${api.discovery.rambling-house.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
