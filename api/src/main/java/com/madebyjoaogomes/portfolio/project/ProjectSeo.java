package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.SiteUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/** Metadata the site turns into the case page's meta tags and schema.org CreativeWork. */
public record ProjectSeo(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank @SiteUrl String image,
        @NotNull @Positive Integer imageWidth,
        @NotNull @Positive Integer imageHeight,
        @NotBlank String imageAlt,
        @NotBlank String name,
        @NotBlank String headline,
        @NotBlank String summary,
        @NotNull List<@NotBlank String> about) {
}
