package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.SiteUrl;
import jakarta.validation.constraints.NotBlank;

public record CaseFeature(@NotBlank String title, @NotBlank String desc, @SiteUrl String image) {
}
