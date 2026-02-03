package com.ColumbusEventAlertService.refactor.util;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static LocalDate parseMonthDayWithYear(String dateText, String pattern) {
        //Jan 20 becomes January 20
        //Do not count the month of "May" as abbreviated
        if(dateText.substring(3, 4).equals(" ") && !dateText.substring(0, 3).equalsIgnoreCase("may")) {
          String fullMonthName = getFullMonthName(dateText.substring(0,3));
          dateText = fullMonthName + dateText.substring(3);
        }

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

    private static String getFullMonthName(String monthName) {
        return switch (monthName.toLowerCase()) {
            case "jan" -> "January";
            case "feb" -> "February";
            case "mar" -> "March";
            case "apr" -> "April";
            case "jun" -> "June";
            case "jul" -> "July";
            case "aug" -> "August";
            case "sep" -> "September";
            case "oct" -> "October";
            case "nov" -> "November";
            case "dec" -> "December";
            default -> throw new IllegalArgumentException("Invalid month name: " + monthName);
        };

    }

    static String monthNameStartsWithUpperCase(String dateText) {
        dateText = dateText.toLowerCase();
         return dateText.substring(0, 1).toUpperCase() + dateText.substring(1);
    }

}
