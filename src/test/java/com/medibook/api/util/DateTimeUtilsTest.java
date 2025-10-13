package com.medibook.api.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Test class for DateTimeUtils
 */
class DateTimeUtilsTest {
    
    @Test
    void testFormatDateWithArgentinaTimezone() {
        // Create a date time in UTC (13:00 UTC should be 10:00 in Argentina during standard time)
        OffsetDateTime utcDateTime = OffsetDateTime.of(2025, 10, 20, 13, 0, 0, 0, ZoneOffset.UTC);
        
        String formattedDate = DateTimeUtils.formatDate(utcDateTime);
        assertEquals("20/10/2025", formattedDate);
    }
    
    @Test
    void testFormatTimeWithArgentinaTimezone() {
        // Create a date time in UTC (13:00 UTC should be 10:00 in Argentina during standard time)
        OffsetDateTime utcDateTime = OffsetDateTime.of(2025, 10, 20, 13, 0, 0, 0, ZoneOffset.UTC);
        
        String formattedTime = DateTimeUtils.formatTime(utcDateTime);
        assertEquals("10:00", formattedTime);
    }
    
    @Test
    void testFormatDateTimeWithArgentinaTimezone() {
        // Create a date time in UTC (13:00 UTC should be 10:00 in Argentina during standard time)
        OffsetDateTime utcDateTime = OffsetDateTime.of(2025, 10, 20, 13, 0, 0, 0, ZoneOffset.UTC);
        
        String formattedDateTime = DateTimeUtils.formatDateTime(utcDateTime);
        assertEquals("20/10/2025 10:00", formattedDateTime);
    }
    
    @Test
    void testFormatDateISOWithArgentinaTimezone() {
        // Create a date time in UTC (13:00 UTC should be 10:00 in Argentina during standard time)
        OffsetDateTime utcDateTime = OffsetDateTime.of(2025, 10, 20, 13, 0, 0, 0, ZoneOffset.UTC);
        
        String formattedDateISO = DateTimeUtils.formatDateISO(utcDateTime);
        assertEquals("2025-10-20", formattedDateISO);
    }
    
    @Test
    void testFormatTimeISOWithArgentinaTimezone() {
        // Create a date time in UTC (13:00 UTC should be 10:00 in Argentina during standard time)
        OffsetDateTime utcDateTime = OffsetDateTime.of(2025, 10, 20, 13, 0, 0, 0, ZoneOffset.UTC);
        
        String formattedTimeISO = DateTimeUtils.formatTimeISO(utcDateTime);
        assertEquals("10:00", formattedTimeISO); // LocalTime.toString() doesn't include seconds when they are zero
    }
    
    @Test
    void testWithNullDateTime() {
        assertEquals("", DateTimeUtils.formatDate(null));
        assertEquals("", DateTimeUtils.formatTime(null));
        assertEquals("", DateTimeUtils.formatDateTime(null));
        assertEquals("", DateTimeUtils.formatDateISO(null));
        assertEquals("", DateTimeUtils.formatTimeISO(null));
    }
    
    @Test
    void testWithArgentinaDateTime() {
        // Create a date time already in Argentina timezone
        ZoneId argentinaZone = ZoneId.of("America/Argentina/Buenos_Aires");
        OffsetDateTime argentinaDateTime = OffsetDateTime.of(2025, 10, 20, 10, 0, 0, 0, 
            argentinaZone.getRules().getOffset(java.time.LocalDateTime.of(2025, 10, 20, 10, 0)));
        
        String formattedTime = DateTimeUtils.formatTime(argentinaDateTime);
        assertEquals("10:00", formattedTime);
        
        String formattedDate = DateTimeUtils.formatDate(argentinaDateTime);
        assertEquals("20/10/2025", formattedDate);
    }
}