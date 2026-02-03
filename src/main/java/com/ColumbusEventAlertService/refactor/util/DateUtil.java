package com.ColumbusEventAlertService.refactor.util;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static LocalDate parseMonthDayWithYear(String dateText, String pattern) {
        LocalDate date;
        try {
            dateText = monthNameStartsWithUpperCase(dateText);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            MonthDay monthDay = MonthDay.parse(dateText, formatter);
            int currentYear = Year.now().getValue();
            date = monthDay.atYear(currentYear);

            if (date.isBefore(LocalDate.now())) {
                date = date.plusYears(1);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to parse date '%s' with pattern '%s': %s",
                            dateText, pattern, e.getMessage()),
                    e
            );
        }
        return date;
    }

    static String monthNameStartsWithUpperCase(String dateText) {
        dateText = dateText.toLowerCase();
         return dateText.substring(0, 1).toUpperCase() + dateText.substring(1);
    }

}
