package com.madebyjoaogomes.portfolio.project;

import jakarta.validation.constraints.NotBlank;

public record CaseFeature(@NotBlank String title, @NotBlank String desc, String image) {
}
