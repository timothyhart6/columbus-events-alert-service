package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConventionCenterStrategy extends AbstractDiscoveryStrategy {

    public ConventionCenterStrategy(
            @Value("${api.discovery.convention.source-name}") String venueName,
            @Value("${api.discovery.convention.venue-id}") String venueId) {
        super(venueName, venueId, true, false);
    }

}
