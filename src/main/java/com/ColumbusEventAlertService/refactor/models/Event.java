package com.ColumbusEventAlertService.refactor.models;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Getter
public class Event {

    private final String locationName;
    private final String name;
    private final LocalDate date;
    private final Optional<String> time;
    private final boolean trafficCausing;
    private final boolean interesting;

    private Event(Builder builder) {
        this.locationName = Objects.requireNonNull(builder.locationName);
        this.name = Objects.requireNonNull(builder.name);
        this.date = Objects.requireNonNull(builder.date);
        this.time = Optional.ofNullable(builder.time);
        this.trafficCausing = builder.trafficCausing;
        this.interesting = builder.interesting;
    }

    public static Builder builder() {
        return new Builder();
    }

    //Could switch to Lombok builder. Creating manually first to understand how it works under the hood).
    public static class Builder {
        private String locationName;
        private String name;
        private LocalDate date;
        private String time;
        private boolean trafficCausing;
        private boolean interesting;

        public Builder locationName(String locationName) {
            this.locationName = locationName;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder time(String time) {
            this.time = time;
            return this;
        }

        public Builder trafficCausing(boolean trafficCausing) {
            this.trafficCausing = trafficCausing;
            return this;
        }

        public Builder interesting(boolean interesting) {
            this.interesting = interesting;
            return this;
        }

        public Event build() {
            return new Event(this);
        }
    }
}
