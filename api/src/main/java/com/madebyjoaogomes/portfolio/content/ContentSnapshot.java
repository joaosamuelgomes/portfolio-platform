package com.madebyjoaogomes.portfolio.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Everything published, in the order the site shows it. See docs/adr/0003-formato-do-snapshot.md. */
public record ContentSnapshot(
        @Schema(requiredMode = REQUIRED, description = "Muda apenas em alterações incompatíveis do formato")
        int schemaVersion,
        @Schema(requiredMode = REQUIRED) Instant generatedAt,
        @Schema(requiredMode = REQUIRED, description = "Por ordem de exibição") List<PublishedProject> projects,
        @Schema(requiredMode = REQUIRED, description = "Mais recentes primeiro") List<PublishedArticle> articles) {

    public static final int SCHEMA_VERSION = 1;
}
