package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.SiteUrl;
import jakarta.validation.constraints.NotBlank;

public record CaseImage(@NotBlank @SiteUrl String src, @NotBlank String alt, @NotBlank String label) {
}
