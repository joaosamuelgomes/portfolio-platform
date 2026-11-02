package com.madebyjoaogomes.portfolio.project;

import jakarta.validation.constraints.NotBlank;

public record CaseTestimonial(
        @NotBlank String quote,
        @NotBlank String author,
        @NotBlank String role,
        @NotBlank String image,
        @NotBlank String imageAlt,
        @NotBlank String source) {
}
