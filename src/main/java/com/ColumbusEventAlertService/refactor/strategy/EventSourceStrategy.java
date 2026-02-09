package com.ColumbusEventAlertService.refactor.strategy;

import com.ColumbusEventAlertService.refactor.models.Event;

import java.util.List;

public interface EventSourceStrategy {

    List<Event> fetchCurrentDayEvents() throws Exception;

    String getLocationName();

}
