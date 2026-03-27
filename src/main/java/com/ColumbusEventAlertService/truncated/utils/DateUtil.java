package com.ColumbusEventAlertService.truncated.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {
    public String convertMonthNameToNumber(String monthName) {
        return switch (monthName.toLowerCase()) {
            case "jan", "january" -> "01";
            case "feb", "february" -> "02";
            case "mar", "march" -> "03";
            case "apr", "april" -> "04";
            case "may" -> "05";
            case "jun", "june" -> "06";
            case "jul", "july" -> "07";
            case "aug", "august" -> "08";
            case "sep", "september" -> "09";
            case "oct", "october" -> "10";
            case "nov", "november" -> "11";
            case "dec", "december" -> "12";
            default -> "Could not map month: " + monthName;
        };
    }

    public String formatDay(String day) {
        return (day.length() == 1) ? "0" + day : day;
    }

    public String getYear() {
        return String.valueOf(Year.now());
    }

    public String getMonth() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
        return LocalDate.now().format(formatter);
    }

    public String getDay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
        return LocalDate.now().format(formatter);
    }

    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public String getCurrentDateFormatted() {
        ZoneId zone = ZoneId.of("America/New_York");
        String today = Instant.now().atZone(zone).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        return today;
    }
}