package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchottensteinCenterStrategy extends AbstractDiscoveryStrategy {

    public SchottensteinCenterStrategy(
            @Value("${api.discovery.schottenstein.source-name}") String venueName,
            @Value("${api.discovery.schottenstein.venue-id}") String venueId) {
        super(venueName, venueId, true, false);
    }

}
