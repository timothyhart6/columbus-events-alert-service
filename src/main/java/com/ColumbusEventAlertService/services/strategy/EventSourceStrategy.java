package com.ColumbusEventAlertService.services.strategy;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;

import java.util.List;

public interface EventSourceStrategy {

    List<Event> fetchCurrentDayEvents() throws EventFetchException;

    String getSourceName();

}
