package com.madebyjoaogomes.portfolio.article;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.madebyjoaogomes.portfolio.content.SiteUrl;
import com.madebyjoaogomes.portfolio.content.Slugs;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * The article's frontmatter plus its Markdown body. Field names follow the site's frontmatter;
 * {@code publishedAt} may stay empty on drafts and is filled in when the article is published.
 */
public record ArticleRequest(
        @NotBlank @Size(max = 120) @Pattern(regexp = Slugs.PATTERN) String slug,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String description,
        @NotBlank @Size(max = 200_000) String markdown,
        List<@NotBlank String> tags,
        @Size(max = 500) @SiteUrl String image,
        @Positive Integer imageWidth,
        @Positive Integer imageHeight,
        @Size(max = 300) String imageAlt,
        LocalDate publishedAt,
        LocalDate updatedAt) {

    private static final java.util.regex.Pattern H1 = java.util.regex.Pattern.compile("^# ", java.util.regex.Pattern.MULTILINE);

    public ArticleRequest {
        tags = tags == null ? List.of() : tags;
    }

    @JsonIgnore
    @AssertTrue(message = "o título é o h1 da página; comece os títulos do corpo em \"##\"")
    public boolean isMarkdownWithoutH1() {
        return markdown == null || !H1.matcher(markdown).find();
    }

    @JsonIgnore
    @AssertTrue(message = "\"image\" exige \"imageWidth\" e \"imageHeight\"")
    public boolean isImageSized() {
        return image == null || (imageWidth != null && imageHeight != null);
    }
}
