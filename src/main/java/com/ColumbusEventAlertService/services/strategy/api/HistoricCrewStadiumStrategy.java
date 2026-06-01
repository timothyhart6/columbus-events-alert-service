package com.ColumbusEventAlertService.services.strategy.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HistoricCrewStadiumStrategy extends AbstractDiscoveryStrategy {

    public HistoricCrewStadiumStrategy(
            @Value("${api.discovery.historic-crew-stadium.source-name}") String venueName,
            @Value("${api.discovery.historic-crew-stadium.venue-id}") String venueId) {
        super(venueName, venueId, true, false);
    }

}
