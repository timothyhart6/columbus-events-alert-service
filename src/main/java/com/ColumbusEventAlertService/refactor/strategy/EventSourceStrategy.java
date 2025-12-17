package com.ColumbusEventAlertService.refactor.strategy;

import com.ColumbusEventAlertService.models.Event;

import java.util.List;

public interface EventSourceStrategy {

    List<Event> getTodaysEvents() throws Exception;

    String getName();

}
