package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MershonAuditoriumStrategy extends AbstractDiscoveryStrategy {

    public MershonAuditoriumStrategy(
            @Value("${api.discovery.mershon.source-name}") String venueName,
            @Value("${api.discovery.mershon.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
