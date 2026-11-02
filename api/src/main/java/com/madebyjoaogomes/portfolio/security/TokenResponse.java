package com.madebyjoaogomes.portfolio.security;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TokenResponse(
        @Schema(requiredMode = REQUIRED) String accessToken,
        @Schema(requiredMode = REQUIRED, example = "Bearer") String tokenType,
        @Schema(requiredMode = REQUIRED, description = "Validade do token em segundos") long expiresIn,
        @Schema(requiredMode = REQUIRED) List<Role> roles) {
}
