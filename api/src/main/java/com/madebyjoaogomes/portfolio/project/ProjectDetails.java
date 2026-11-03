package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.SiteUrl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** The case study page. Stored as JSONB: it is always read and written as one block. */
public record ProjectDetails(
        @NotBlank String heroSubtitle,
        @NotBlank String role,
        @NotBlank String timeline,
        @NotBlank String platform,
        @NotBlank String sector,
        @NotBlank String challenge,
        @NotBlank @SiteUrl String challengeImage,
        @NotBlank String challengeImageAlt,
        @NotBlank String challengeImageLabel,
        @NotNull List<@NotBlank String> painPoints,
        @NotBlank String strategy,
        @NotBlank @SiteUrl String strategyImage,
        @NotBlank String strategyImageAlt,
        @NotBlank String strategyImageLabel,
        @NotBlank @SiteUrl String solutionImage,
        @NotBlank String solutionImageAlt,
        @NotBlank String solutionImageLabel,
        @NotNull List<@Valid @NotNull CaseFeature> features,
        @NotNull List<@Valid @NotNull CaseImage> additionalImages,
        @NotNull List<@Valid @NotNull CaseResult> results,
        @Valid CaseTestimonial testimonial,
        @NotBlank String conclusion) {
}
