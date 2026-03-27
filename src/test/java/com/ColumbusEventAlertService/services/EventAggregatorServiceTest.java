package com.ColumbusEventAlertService.services;

import com.ColumbusEventAlertService.exception.EventFetchException;
import com.ColumbusEventAlertService.models.Event;
import com.ColumbusEventAlertService.services.strategy.EventSourceStrategy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventAggregatorServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 11);

    @Nested
    class RemoveDuplicates {

        EventAggregatorService service = new EventAggregatorService(List.of());

        @Test
        void emptyList_returnsEmptyList() {
            assertTrue(service.removeDuplicates(List.of()).isEmpty());
        }

        @Test
        void noDuplicates_returnsAllEvents() {
            Event concert  = event("O2",      "Concert",  TODAY, null);
            Event football = event("Wembley", "Football", TODAY, null);

            assertEquals(2, service.removeDuplicates(List.of(concert, football)).size());
        }

        @Test
        void duplicateWhereOneHasTime_keepsTheOneWithTime() {
            Event withoutTime = event("O2", "Concert", TODAY, null);
            Event withTime    = event("O2", "Concert", TODAY, "8:00pm");

            List<Event> result = service.removeDuplicates(List.of(withoutTime, withTime));

            assertEquals(1, result.size());
            assertEquals("8:00pm", result.get(0).getTime());
        }

        @Test
        void duplicatesBothHaveTime_keepsFirstEncountered() {
            Event first  = event("O2", "Concert", TODAY, "7:00pm");
            Event second = event("O2", "Concert", TODAY, "8:00pm");

            List<Event> result = service.removeDuplicates(List.of(first, second));

            assertEquals(1, result.size());
            assertEquals("7:00pm", result.get(0).getTime());
        }

        @Test
        void sameNameDifferentDate_notConsideredDuplicate() {
            Event first  = event("O2", "Concert", TODAY,             null);
            Event second = event("O2", "Concert", TODAY.plusDays(1), null);

            assertEquals(2, service.removeDuplicates(List.of(first, second)).size());
        }

        @Test
        void sameNameDifferentLocation_notConsideredDuplicate() {
            Event first  = event("O2",      "Concert", TODAY, null);
            Event second = event("Wembley", "Concert", TODAY, null);

            assertEquals(2, service.removeDuplicates(List.of(first, second)).size());
        }
    }

    @Nested
    class GetCurrentDayEvents {

        @Test
        void noSources_returnsEmptyList() {
            EventAggregatorService service = new EventAggregatorService(List.of());

            assertTrue(service.getCurrentDayEvents().isEmpty());
        }

        @Test
        void allSourcesEmpty_returnsEmptyList() {
            EventAggregatorService service = new EventAggregatorService(List.of(
                    stubSource(List.of()),
                    stubSource(List.of())
            ));

            assertTrue(service.getCurrentDayEvents().isEmpty());
        }

        @Test
        void singleSource_returnsItsEvents() {
            Event concert = event("O2", "Concert", TODAY, null);
            EventAggregatorService service = new EventAggregatorService(List.of(
                    stubSource(List.of(concert))
            ));

            List<Event> result = service.getCurrentDayEvents();

            assertEquals(1, result.size());
            assertEquals(concert, result.get(0));
        }

        @Test
        void multipleSources_aggregatesAllEvents() {
            Event concert  = event("O2",      "Concert",  TODAY, null);
            Event football = event("Wembley", "Football", TODAY, null);

            EventAggregatorService service = new EventAggregatorService(List.of(
                    stubSource(List.of(concert)),
                    stubSource(List.of(football))
            ));

            assertEquals(2, service.getCurrentDayEvents().size());
        }

        @Test
        void singleSourceMultipleEvents_returnsAllEvents() {
            Event morningShow = event("O2", "Morning Show", TODAY, "11:00am");
            Event eveningShow = event("O2", "Evening Show", TODAY, "6:00pm");

            EventAggregatorService service = new EventAggregatorService(List.of(
                    stubSource(List.of(morningShow, eveningShow))
            ));

            assertEquals(2, service.getCurrentDayEvents().size());
        }

        @Test
        void singleSourceMultipleEventsAndSecondSourceOneEvent_returnsAllThreeEvents() {
            Event morningShow = event("O2", "Morning Show", TODAY, "11:00am");
            Event eveningShow = event("O2", "Evening Show", TODAY, "6:00pm");
            Event concert     = event("Wembley", "Concert", TODAY, null);

            EventAggregatorService service = new EventAggregatorService(List.of(
                    stubSource(List.of(morningShow, eveningShow)),
                    stubSource(List.of(concert))
            ));

            assertEquals(3, service.getCurrentDayEvents().size());
        }

        @Test
        void duplicateAcrossSources_deduplicatesAndPrioritisesEventWithTime() {
            Event fromDb       = event("O2", "Concert", TODAY, null);
            Event fromScraping = event("O2", "Concert", TODAY, "8:00pm");

            EventAggregatorService service = new EventAggregatorService(List.of(
                    stubSource(List.of(fromDb)),
                    stubSource(List.of(fromScraping))
            ));

            List<Event> result = service.getCurrentDayEvents();

            assertEquals(1, result.size());
            assertEquals("8:00pm", result.get(0).getTime());
        }

        @Test
        void oneFetchThrows_stillReturnsEventsFromOtherSource() {
            Event concert = event("O2", "Concert", TODAY, null);

            EventAggregatorService service = new EventAggregatorService(List.of(
                    throwingSource(),
                    stubSource(List.of(concert))
            ));

            List<Event> result = service.getCurrentDayEvents();

            assertEquals(1, result.size());
            assertEquals(concert, result.get(0));
        }

        @Test
        void allSourcesThrow_returnsEmptyList() {
            EventAggregatorService service = new EventAggregatorService(List.of(
                    throwingSource(),
                    throwingSource()
            ));

            assertTrue(service.getCurrentDayEvents().isEmpty());
        }
    }

    // --- helpers ---

    private Event event(String locationName, String name, LocalDate date, String time) {
        return Event.builder()
                .locationName(locationName)
                .name(name)
                .date(date)
                .time(time)
                .build();
    }

    private EventSourceStrategy stubSource(List<Event> events) {
        return new EventSourceStrategy() {
            @Override
            public List<Event> fetchCurrentDayEvents() {
                return events;
            }

            @Override
            public String getSourceName() {
                return "stub";
            }
        };
    }

    private EventSourceStrategy throwingSource() {
        return new EventSourceStrategy() {
            @Override
            public List<Event> fetchCurrentDayEvents() throws EventFetchException {
                throw new EventFetchException("throwing-stub", EventFetchException.ErrorType.UNKNOWN_ERROR, "stub error");
            }

            @Override
            public String getSourceName() {
                return "throwing-stub";
            }
        };
    }
}