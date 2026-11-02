package com.madebyjoaogomes.portfolio.site;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SiteDeployServiceTest {

    private static final URI HOOK = URI.create("https://api.vercel.com/v1/integrations/deploy/prj_test/hook");
    private static final Instant NOW = Instant.parse("2026-11-02T21:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private RestClient.Builder http;
    private MockRestServiceServer hook;

    @BeforeEach
    void setUp() {
        http = RestClient.builder();
        hook = MockRestServiceServer.bindTo(http).build();
    }

    @Test
    void postsToTheDeployHookAndRemembersWhen() {
        hook.expect(requestTo(HOOK)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess());
        var service = new SiteDeployService(new SiteProperties(HOOK), http, CLOCK);

        assertThat(service.status()).isEqualTo(new SiteStatus(true, null));
        assertThat(service.requestDeploy()).isEqualTo(new SiteStatus(true, NOW));
        hook.verify();
    }

    @Test
    void refusesWithoutAHookAndCallsNothing() {
        var service = new SiteDeployService(new SiteProperties(null), http, CLOCK);

        assertThat(service.status().deployConfigured()).isFalse();
        assertThatThrownBy(service::requestDeploy)
                .isInstanceOfSatisfying(SiteDeployException.class, error -> assertThat(error.isConfigured()).isFalse());
        hook.verify();
    }

    @Test
    void reportsAFailedHookWithoutRecordingTheRequest() {
        hook.expect(requestTo(HOOK)).andRespond(withServerError());
        var service = new SiteDeployService(new SiteProperties(HOOK), http, CLOCK);

        assertThatThrownBy(service::requestDeploy)
                .isInstanceOfSatisfying(SiteDeployException.class, error -> assertThat(error.isConfigured()).isTrue());
        assertThat(service.status().lastDeployRequestedAt()).isNull();
    }
}
