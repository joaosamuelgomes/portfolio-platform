package com.madebyjoaogomes.portfolio.project;

import jakarta.validation.constraints.NotBlank;

/** The four-line pitch shown on the project card. */
public record ProjectSummary(
        @NotBlank String problem,
        @NotBlank String solution,
        @NotBlank String contribution,
        @NotBlank String proof) {
}
