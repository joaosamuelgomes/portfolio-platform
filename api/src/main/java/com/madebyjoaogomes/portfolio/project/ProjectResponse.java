package com.madebyjoaogomes.portfolio.project;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record ProjectResponse(
        @Schema(requiredMode = REQUIRED) Long id,
        @Schema(requiredMode = REQUIRED) String slug,
        @Schema(requiredMode = REQUIRED) PublicationStatus status,
        @Schema(requiredMode = REQUIRED) int order,
        @Schema(requiredMode = REQUIRED) String title,
        @Schema(requiredMode = REQUIRED) String category,
        @Schema(requiredMode = REQUIRED) String thumbnail,
        String logo,
        @Schema(requiredMode = REQUIRED) ProjectSummary summary,
        @Schema(requiredMode = REQUIRED) ProjectDetails details,
        @Schema(requiredMode = REQUIRED) ProjectSeo seo,
        @Schema(requiredMode = REQUIRED) Instant createdAt,
        @Schema(requiredMode = REQUIRED) Instant modifiedAt) {

    static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getSlug(), project.getStatus(), project.getDisplayOrder(),
                project.getTitle(), project.getCategory(), project.getThumbnail(), project.getLogo(),
                project.getSummary(), project.getDetails(), project.getSeo(),
                project.getCreatedAt(), project.getUpdatedAt());
    }
}
