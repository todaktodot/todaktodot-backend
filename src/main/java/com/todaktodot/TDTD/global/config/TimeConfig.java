package com.todaktodot.TDTD.global.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeConfig {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    @PostConstruct
    public void setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ASIA_SEOUL));
    }

    @Bean
    public Clock clock() {
        return Clock.system(ASIA_SEOUL);
    }
}
