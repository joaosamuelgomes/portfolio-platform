package com.madebyjoaogomes.portfolio.site;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The static site that publishes this content. {@code deployHookUrl} is a Vercel Deploy Hook:
 * a POST to it rebuilds the site, which reads GET /public/content during the build. It is a
 * secret (anyone holding it can trigger builds) and optional: without it the deploy is disabled.
 */
@ConfigurationProperties("app.site")
public record SiteProperties(URI deployHookUrl) {

    boolean deployConfigured() {
        return deployHookUrl != null && !deployHookUrl.toString().isBlank();
    }
}
