package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PalaceTheatreStrategy extends AbstractDiscoveryStrategy {

    public PalaceTheatreStrategy(
            @Value("${api.discovery.palace-theatre.source-name}") String venueName,
            @Value("${api.discovery.palace-theatre.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
