package com.madebyjoaogomes.portfolio.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.madebyjoaogomes.portfolio.article.Article;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * An article as the site's {@code content/articles/*.md} files describe it: the frontmatter fields
 * plus the raw Markdown body. The site renders the HTML and computes the reading time.
 */
public record PublishedArticle(
        @Schema(requiredMode = REQUIRED) String slug,
        @Schema(requiredMode = REQUIRED) String title,
        @Schema(requiredMode = REQUIRED) String description,
        @Schema(requiredMode = REQUIRED) LocalDate publishedAt,
        LocalDate updatedAt,
        @Schema(requiredMode = REQUIRED) List<String> tags,
        String image,
        Integer imageWidth,
        Integer imageHeight,
        String imageAlt,
        @Schema(requiredMode = REQUIRED) String markdown) {

    static PublishedArticle from(Article article) {
        return new PublishedArticle(article.getSlug(), article.getTitle(), article.getDescription(),
                article.getPublishedOn(), article.getRevisedOn(), article.getTags(), article.getImage(),
                article.getImageWidth(), article.getImageHeight(), article.getImageAlt(), article.getBodyMarkdown());
    }
}
