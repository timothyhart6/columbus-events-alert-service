package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LincolnTheatreStrategy extends AbstractDiscoveryStrategy {

    public LincolnTheatreStrategy(
            @Value("${api.discovery.lincoln-theatre.source-name}") String venueName,
            @Value("${api.discovery.lincoln-theatre.venue-id}") String venueId) {
        super(venueName, venueId, false, true);
    }

}
