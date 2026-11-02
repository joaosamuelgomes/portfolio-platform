package com.madebyjoaogomes.portfolio.project;

import jakarta.validation.constraints.NotBlank;

public record CaseResult(@NotBlank String value, @NotBlank String label) {
}
