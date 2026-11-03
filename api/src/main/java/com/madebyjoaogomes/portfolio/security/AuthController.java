package com.madebyjoaogomes.portfolio.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Emissão de tokens para o painel admin")
@SecurityRequirements
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final TokenService tokens;
    private final LoginAttemptLimiter limiter;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokens, LoginAttemptLimiter limiter) {
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
        this.limiter = limiter;
    }

    @PostMapping("/token")
    @Operation(operationId = "createToken", summary = "Troca usuário e senha por um JWT")
    public TokenResponse token(@Valid @RequestBody TokenRequest request, HttpServletRequest http) {
        // Behind Caddy, the forwarded-headers filter has already put the client's address here.
        var client = http.getRemoteAddr();
        limiter.check(client);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (AuthenticationException exception) {
            limiter.recordFailure(client);
            log.warn("Failed login for '{}' from {}", request.username(), client);
            throw exception;
        }
        limiter.recordSuccess(client);
        log.info("Login for '{}' from {}", authentication.getName(), client);
        return tokens.issue(authentication);
    }
}
