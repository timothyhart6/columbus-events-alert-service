package com.ColumbusEventAlertService.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilTest {

    private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MMMM d");
 
    @ParameterizedTest
    @ValueSource(strings = {"January 1", "February 20", "July 10", "December 31"})
    @DisplayName("should parse valid month-day dates")
    void shouldParseValidDates(String dateText) {
        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertNotNull(result);

        // Parse expected month and day
        MonthDay expected = MonthDay.parse(dateText, MONTH_DAY);
        assertEquals(expected.getMonth(), result.getMonth());
        assertEquals(expected.getDayOfMonth(), result.getDayOfMonth());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Jan 1", "Feb 20", "Mar 07", "Apr 22", "May 1", "Jun 19", "Jul 10", "Aug 30", "Sep 17", "Oct 18", "Nov 4", "Dec 25"})
    @DisplayName("should handle abbreviated month names")
    void shouldHandleAbbreviatedMonthNames(String dateText) {
        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertNotNull(result);
    }

    @Test
    void shouldParseUppercaseMonths() {
        String dateText = "MARCH 20";
        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertNotNull(result);

        // Parse expected month and day
        MonthDay expected = MonthDay.parse(DateUtil.monthNameStartsWithUpperCase(dateText), MONTH_DAY);
        assertEquals(expected.getMonth(), result.getMonth());
        assertEquals(expected.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void shouldParseLowercaseMonths() {
        String dateText = "june 20";
        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertNotNull(result);

        // Parse expected month and day
        MonthDay expected = MonthDay.parse(DateUtil.monthNameStartsWithUpperCase(dateText), MONTH_DAY);
        assertEquals(expected.getMonth(), result.getMonth());
        assertEquals(expected.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void shouldParseMixedCasedMonths() {
        String dateText = "SePTEmbEr 20";
        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertNotNull(result);

        // Parse expected month and day
        MonthDay expected = MonthDay.parse(DateUtil.monthNameStartsWithUpperCase(dateText), MONTH_DAY);
        assertEquals(expected.getMonth(), result.getMonth());
        assertEquals(expected.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void shouldParseSingleDigitDay() {
        String dateText = "March 8";
        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertNotNull(result);

        // Parse expected month and day
        MonthDay expected = MonthDay.parse(DateUtil.monthNameStartsWithUpperCase(dateText), MONTH_DAY);
        assertEquals(expected.getMonth(), result.getMonth());
        assertEquals(expected.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void shouldUseCurrentYearForFutureDates() {
        LocalDate futureDate = LocalDate.now().plusDays(30);
        String dateText = futureDate.format(MONTH_DAY);

        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertEquals(futureDate.getYear(), result.getYear());
        assertEquals(futureDate.getMonth(), result.getMonth());
        assertEquals(futureDate.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void shouldUseNextYearForDatesInThePast() {
        LocalDate pastDate = LocalDate.now().minusDays(30);
        String dateText = pastDate.format(MONTH_DAY);

        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertEquals(LocalDate.now().getYear() + 1, result.getYear());
        assertEquals(pastDate.getMonth(), result.getMonth());
        assertEquals(pastDate.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void shouldHandleYearRollover() {
        // This test verifies the logic when it's December and event is in January
        String dateText = "January 15";

        LocalDate result = DateUtil.parseMonthDayWithYear(dateText, "MMMM d");

        assertEquals(1, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());

        // Year should be current or next depending on if January has passed
        LocalDate now = LocalDate.now();
        LocalDate janInCurrentYear = LocalDate.of(now.getYear(), 1, 15);

        if (janInCurrentYear.isBefore(now)) {
            assertEquals(now.getYear() + 1, result.getYear());
        } else {
            assertEquals(now.getYear(), result.getYear());
        }
    }

    @Test
    void shouldThrowWhenDateFormatInvalid() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.parseMonthDayWithYear("invalid date", "MMMM d"));
    }

    @Test
    void shouldThrowWhenDateIsNumeric() {
        assertThrows(RuntimeException.class, () -> DateUtil.parseMonthDayWithYear("01-15", "MMMM d"));
    }

    @Test
    void shouldThrowWhenDateEmpty() {
        assertThrows(RuntimeException.class, () -> DateUtil.parseMonthDayWithYear("", "MMMM d"));
    }

    @Test
    void shouldThrowWhenDateNull() {
        assertThrows(RuntimeException.class, () -> DateUtil.parseMonthDayWithYear(null, "MMMM d"));
    }

    @Test
    void shouldThrowWhenInvalidMonthAbbreviation() {
        assertThrows(RuntimeException.class, () -> DateUtil.parseMonthDayWithYear("Sma 17", "MMMM d"));
    }
}
