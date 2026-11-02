package com.madebyjoaogomes.portfolio.site;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record SiteStatus(
        @Schema(description = "Se SITE_DEPLOY_HOOK_URL está configurada")
        boolean deployConfigured,
        @Schema(description = "Último pedido de deploy desde que a API subiu; ausente se não houve")
        Instant lastDeployRequestedAt) {
}
