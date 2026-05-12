package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LowerFieldStrategy extends AbstractDiscoveryStrategy {

    public LowerFieldStrategy(
            @Value("${api.discovery.lower.source-name}") String venueName,
            @Value("${api.discovery.lower.venue-id}") String venueId) {
        super(venueName, venueId, true, true);
    }

}
