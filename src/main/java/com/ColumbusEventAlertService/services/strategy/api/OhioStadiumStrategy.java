package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OhioStadiumStrategy extends AbstractDiscoveryStrategy {

    public OhioStadiumStrategy(
            @Value("${api.discovery.ohio-stadium.source-name}") String venueName,
            @Value("${api.discovery.ohio-stadium.venue-id}") String venueId) {
        super(venueName, venueId, true, false);
    }

}
