package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OhioStateFairgroundsStrategy extends AbstractDiscoveryStrategy {

    public OhioStateFairgroundsStrategy(
            @Value("${api.discovery.fairgrounds.source-name}") String venueName,
            @Value("${api.discovery.fairgrounds.venue-id}") String venueId) {
        super(venueName, venueId, true, true);
    }

}
