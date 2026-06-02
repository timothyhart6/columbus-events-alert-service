package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OhioTheatreStrategy extends AbstractDiscoveryStrategy {

    public OhioTheatreStrategy(
            @Value("${api.discovery.ohio-theatre.source-name}") String venueName,
            @Value("${api.discovery.ohio-theatre.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
