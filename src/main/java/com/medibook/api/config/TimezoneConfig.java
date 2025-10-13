package com.medibook.api.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimezoneConfig {
    
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Argentina/Buenos_Aires")));
    }
}