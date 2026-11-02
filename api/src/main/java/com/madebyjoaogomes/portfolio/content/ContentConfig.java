package com.madebyjoaogomes.portfolio.content;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentConfig {

    /** Editorial dates ("published today") follow the author's time zone, not the server's. */
    @Bean
    Clock clock(@Value("${app.content.time-zone:America/Sao_Paulo}") ZoneId zone) {
        return Clock.system(zone);
    }
}
