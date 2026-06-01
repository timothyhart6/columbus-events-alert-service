package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SkullysStrategy extends AbstractDiscoveryStrategy {

    public SkullysStrategy(
            @Value("${api.discovery.skullys.source-name}") String venueName,
            @Value("${api.discovery.skullys.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
