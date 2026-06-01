package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RumbaCafeStrategy extends AbstractDiscoveryStrategy {

    public RumbaCafeStrategy(
            @Value("${api.discovery.rumba-cafe.source-name}") String venueName,
            @Value("${api.discovery.rumba-cafe.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
