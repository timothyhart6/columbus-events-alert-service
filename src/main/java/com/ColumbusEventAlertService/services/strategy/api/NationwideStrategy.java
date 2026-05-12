package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NationwideStrategy extends AbstractDiscoveryStrategy{

    public NationwideStrategy(
            @Value("${api.discovery.nationwide.source-name}") String venueName,
            @Value("${api.discovery.nationwide.venue-id}") String venueId) {
        super(venueName, venueId);
    }

}
