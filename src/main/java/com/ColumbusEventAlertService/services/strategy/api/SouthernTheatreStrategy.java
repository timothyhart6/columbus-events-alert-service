package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SouthernTheatreStrategy extends AbstractDiscoveryStrategy {

    public SouthernTheatreStrategy(
            @Value("${api.discovery.southern-theatre.source-name}") String venueName,
            @Value("${api.discovery.southern-theatre.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
