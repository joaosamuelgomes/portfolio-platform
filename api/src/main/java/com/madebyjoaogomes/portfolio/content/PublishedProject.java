package com.madebyjoaogomes.portfolio.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.madebyjoaogomes.portfolio.project.Project;
import com.madebyjoaogomes.portfolio.project.ProjectDetails;
import com.madebyjoaogomes.portfolio.project.ProjectSeo;
import com.madebyjoaogomes.portfolio.project.ProjectSummary;
import io.swagger.v3.oas.annotations.media.Schema;

/** A project as the site's {@code content/projects/*.json} files describe it, minus the publication flag. */
public record PublishedProject(
        @Schema(requiredMode = REQUIRED, description = "Identificador usado na URL /case/{id}") Long id,
        @Schema(requiredMode = REQUIRED) String slug,
        @Schema(requiredMode = REQUIRED) int order,
        @Schema(requiredMode = REQUIRED) String title,
        @Schema(requiredMode = REQUIRED) String category,
        @Schema(requiredMode = REQUIRED) ProjectSummary summary,
        @Schema(requiredMode = REQUIRED) String thumbnail,
        String logo,
        @Schema(requiredMode = REQUIRED) ProjectDetails details,
        @Schema(requiredMode = REQUIRED) ProjectSeo seo) {

    static PublishedProject from(Project project) {
        return new PublishedProject(project.getId(), project.getSlug(), project.getDisplayOrder(), project.getTitle(),
                project.getCategory(), project.getSummary(), project.getThumbnail(), project.getLogo(),
                project.getDetails(), project.getSeo());
    }
}
