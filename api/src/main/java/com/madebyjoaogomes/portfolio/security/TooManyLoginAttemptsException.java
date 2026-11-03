package com.madebyjoaogomes.portfolio.security;

import java.time.Duration;

/** The client failed to log in too many times; it may try again after {@link #retryAfter()}. */
public class TooManyLoginAttemptsException extends RuntimeException {

    private final Duration retryAfter;

    TooManyLoginAttemptsException(Duration retryAfter) {
        super("Muitas tentativas de login; tente de novo em alguns minutos");
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
