package com.madebyjoaogomes.portfolio.project;

import jakarta.validation.constraints.NotBlank;

public record CaseImage(@NotBlank String src, @NotBlank String alt, @NotBlank String label) {
}
