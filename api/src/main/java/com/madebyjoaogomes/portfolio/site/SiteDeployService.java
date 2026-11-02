package com.madebyjoaogomes.portfolio.site;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Asks the static site to rebuild. Publishing is a deliberate step in the panel rather than a
 * side effect of every save, so a batch of edits costs one build and nothing half-edited ships.
 */
@Service
public class SiteDeployService {

    private static final Logger log = LoggerFactory.getLogger(SiteDeployService.class);

    private final SiteProperties site;
    private final RestClient http;
    private final Clock clock;
    /** In memory: a hint for the panel, not an audit trail, so a restart may forget it. */
    private final AtomicReference<Instant> lastRequest = new AtomicReference<>();

    public SiteDeployService(SiteProperties site, RestClient.Builder http, Clock clock) {
        this.site = site;
        // Timeouts come from spring.http.client.* in application.yml.
        this.http = http.build();
        this.clock = clock;
    }

    public SiteStatus status() {
        return new SiteStatus(site.deployConfigured(), lastRequest.get());
    }

    public SiteStatus requestDeploy() {
        if (!site.deployConfigured()) {
            throw SiteDeployException.notConfigured();
        }
        try {
            http.post().uri(site.deployHookUrl()).retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("Site deploy hook failed: {}", exception.getMessage());
            throw SiteDeployException.hookFailed(exception);
        }
        lastRequest.set(clock.instant());
        log.info("Site deploy requested");
        return status();
    }
}
