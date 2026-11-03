package com.madebyjoaogomes.portfolio.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoginAttemptLimiterTest {

    private static final String CLIENT = "198.51.100.4";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-03T12:00:00Z"));
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(new SecurityProperties(
            null, List.of(), List.of(), new SecurityProperties.LoginLimit(3, Duration.ofMinutes(15))), clock);

    @Test
    void blocksAfterTheLimitUntilTheWindowEnds() {
        fail(3);

        assertThatThrownBy(() -> limiter.check(CLIENT))
                .isInstanceOfSatisfying(TooManyLoginAttemptsException.class,
                        exception -> assertThat(exception.retryAfter()).isEqualTo(Duration.ofMinutes(15)));

        clock.advance(Duration.ofMinutes(15));
        assertThatCode(() -> limiter.check(CLIENT)).doesNotThrowAnyException();
    }

    @Test
    void countsEachAddressApart() {
        fail(3);

        assertThatCode(() -> limiter.check("198.51.100.5")).doesNotThrowAnyException();
    }

    @Test
    void successClearsTheFailures() {
        fail(2);
        limiter.recordSuccess(CLIENT);
        fail(2);

        assertThatCode(() -> limiter.check(CLIENT)).doesNotThrowAnyException();
    }

    @Test
    void anExpiredWindowStartsOver() {
        fail(2);
        clock.advance(Duration.ofMinutes(20));
        fail(2);

        assertThatCode(() -> limiter.check(CLIENT)).doesNotThrowAnyException();
    }

    private void fail(int times) {
        for (int i = 0; i < times; i++) {
            limiter.check(CLIENT);
            limiter.recordFailure(CLIENT);
        }
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
