package com.madebyjoaogomes.portfolio.article;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ArticleResponse(
        @Schema(requiredMode = REQUIRED) Long id,
        @Schema(requiredMode = REQUIRED) String slug,
        @Schema(requiredMode = REQUIRED) PublicationStatus status,
        @Schema(requiredMode = REQUIRED) String title,
        @Schema(requiredMode = REQUIRED) String description,
        @Schema(requiredMode = REQUIRED) String markdown,
        @Schema(requiredMode = REQUIRED) List<String> tags,
        String image,
        Integer imageWidth,
        Integer imageHeight,
        String imageAlt,
        LocalDate publishedAt,
        LocalDate updatedAt,
        @Schema(requiredMode = REQUIRED) Instant createdAt,
        @Schema(requiredMode = REQUIRED) Instant modifiedAt) {

    static ArticleResponse from(Article article) {
        return new ArticleResponse(article.getId(), article.getSlug(), article.getStatus(), article.getTitle(),
                article.getDescription(), article.getBodyMarkdown(), article.getTags(), article.getImage(),
                article.getImageWidth(), article.getImageHeight(), article.getImageAlt(),
                article.getPublishedOn(), article.getRevisedOn(), article.getCreatedAt(), article.getUpdatedAt());
    }
}
