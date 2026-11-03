package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.SiteUrl;
import com.madebyjoaogomes.portfolio.content.Slugs;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Everything an editor writes about a project; status and order have their own endpoints. */
public record ProjectRequest(
        @NotBlank @Size(max = 120) @Pattern(regexp = Slugs.PATTERN) String slug,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 200) String category,
        @NotBlank @Size(max = 500) @SiteUrl String thumbnail,
        @Size(max = 500) @SiteUrl String logo,
        @NotNull @Valid ProjectSummary summary,
        @NotNull @Valid ProjectDetails details,
        @NotNull @Valid ProjectSeo seo) {
}
