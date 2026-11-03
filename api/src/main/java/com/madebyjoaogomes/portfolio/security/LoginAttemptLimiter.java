package com.madebyjoaogomes.portfolio.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Caps failed logins per client address. Checking a password costs a bcrypt round, so past the
 * limit the API answers 429 without touching it: that stops guessing and keeps a burst of bad
 * logins from eating the VM's CPU. In memory, per instance; a restart clears it.
 */
@Component
public class LoginAttemptLimiter {

    /** Past this many tracked addresses, expired windows are swept on the next failure. */
    private static final int SWEEP_THRESHOLD = 10_000;

    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Failures> failures = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(SecurityProperties properties, Clock clock) {
        this.maxFailures = properties.loginLimit().maxFailures();
        this.window = properties.loginLimit().window();
        this.clock = clock;
    }

    /** Throws when {@code client} used up its failures in the current window. */
    public void check(String client) {
        var now = clock.instant();
        var current = failures.get(client);
        if (current != null && !current.expired(now, window) && current.count() >= maxFailures) {
            throw new TooManyLoginAttemptsException(Duration.between(now, current.start().plus(window)));
        }
    }

    public void recordFailure(String client) {
        var now = clock.instant();
        if (failures.size() > SWEEP_THRESHOLD) {
            failures.values().removeIf(entry -> entry.expired(now, window));
        }
        failures.merge(client, new Failures(now, 1),
                (old, fresh) -> old.expired(now, window) ? fresh : new Failures(old.start(), old.count() + 1));
    }

    public void recordSuccess(String client) {
        failures.remove(client);
    }

    private record Failures(Instant start, int count) {

        boolean expired(Instant now, Duration window) {
            return !now.isBefore(start.plus(window));
        }
    }
}
