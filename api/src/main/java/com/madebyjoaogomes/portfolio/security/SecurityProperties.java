package com.madebyjoaogomes.portfolio.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Accounts and token settings, all supplied through environment variables. An account whose
 * password is empty is skipped, so a deployment only enables the accounts it configures.
 */
@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @DefaultValue List<Account> users,
        @DefaultValue List<String> corsAllowedOrigins,
        @Valid @DefaultValue LoginLimit loginLimit) {

    /** Failed logins allowed per client address within {@code window}. */
    public record LoginLimit(
            @DefaultValue("10") @Positive int maxFailures,
            @DefaultValue("PT15M") Duration window) {
    }

    public record Jwt(
            @NotBlank(message = "defina JWT_SECRET (32+ caracteres)")
            @Size(min = 32, message = "JWT_SECRET precisa de pelo menos 32 caracteres para HS256")
            String secret,
            @DefaultValue("portfolio-api") String issuer,
            @DefaultValue("PT1H") Duration ttl) {
    }

    public record Account(@NotBlank String username, String password, @NotNull Role role) {

        boolean enabled() {
            return password != null && !password.isBlank();
        }
    }
}
