package com.medibook.api.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    
    public static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public static String formatDate(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.atZoneSameInstant(ARGENTINA_ZONE).format(DATE_FORMATTER);
    }
    
    public static String formatTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.atZoneSameInstant(ARGENTINA_ZONE).format(TIME_FORMATTER);
    }
    
    public static String formatDateISO(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.atZoneSameInstant(ARGENTINA_ZONE).toLocalDate().toString();
    }
    
    public static String formatTimeISO(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.atZoneSameInstant(ARGENTINA_ZONE).toLocalTime().toString();
    }
    
    public static String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return formatDate(dateTime) + " " + formatTime(dateTime);
    }
}