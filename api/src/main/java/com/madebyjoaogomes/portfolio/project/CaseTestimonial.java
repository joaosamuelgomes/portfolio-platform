package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.SiteUrl;
import jakarta.validation.constraints.NotBlank;

public record CaseTestimonial(
        @NotBlank String quote,
        @NotBlank String author,
        @NotBlank String role,
        @NotBlank @SiteUrl String image,
        @NotBlank String imageAlt,
        @NotBlank String source) {
}
