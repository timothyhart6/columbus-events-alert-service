package com.ColumbusEventAlertService.truncated.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Event {
    private String locationName;
    private String eventName;
    private String date;
    private String time;
    private boolean isBadTraffic;
    private boolean isDesiredEvent;

    public Event(String locationName, boolean isBadTraffic, boolean isDesiredEvent) {
        this.locationName = locationName;
        this.isBadTraffic = isBadTraffic;
        this.isDesiredEvent = isDesiredEvent;
    }

    //used for events from the database
    public Event(String locationName, String eventName, String date, String time, boolean badTraffic, boolean desiredEvent) {
        this.locationName = locationName;
        this.eventName = eventName;
        this.date = date;
        this.time = time;
        this.isBadTraffic = badTraffic;
        this.isDesiredEvent = desiredEvent;
    }
}