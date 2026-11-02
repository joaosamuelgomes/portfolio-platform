package com.madebyjoaogomes.portfolio.project;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Every project id, in the order the site should show them. */
public record ProjectOrderRequest(@NotEmpty List<@NotNull Long> projectIds) {
}
