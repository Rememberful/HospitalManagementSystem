package com.hms.hospital.security;

import com.hms.hospital.exception.TooManyAttemptsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class LoginAttemptServiceTest {

    private static class MutableClock extends Clock {
        private Instant now = Instant.parse("2030-01-01T00:00:00Z");
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
        void advance(Duration d) { now = now.plus(d); }
    }

    @Test
    @DisplayName("does not lock out before the failure threshold is reached")
    void noLockoutBeforeThreshold() {
        LoginAttemptService service = new LoginAttemptService(5, Duration.ofMinutes(15), Clock.systemUTC());

        for (int i = 0; i < 4; i++) service.recordFailure("alice");

        assertThatCode(() -> service.checkNotLocked("alice")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("locks out once the failure threshold is reached")
    void locksOutAtThreshold() {
        LoginAttemptService service = new LoginAttemptService(3, Duration.ofMinutes(15), Clock.systemUTC());

        service.recordFailure("alice");
        service.recordFailure("alice");
        service.recordFailure("alice");

        assertThatThrownBy(() -> service.checkNotLocked("alice"))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    @DisplayName("a successful login clears the failure count")
    void successResetsCount() {
        LoginAttemptService service = new LoginAttemptService(3, Duration.ofMinutes(15), Clock.systemUTC());

        service.recordFailure("alice");
        service.recordFailure("alice");
        service.recordSuccess("alice");
        service.recordFailure("alice");
        service.recordFailure("alice");

        assertThatCode(() -> service.checkNotLocked("alice")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("the lockout expires after the configured duration")
    void lockoutExpires() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), clock);

        service.recordFailure("alice");
        service.recordFailure("alice");
        assertThatThrownBy(() -> service.checkNotLocked("alice")).isInstanceOf(TooManyAttemptsException.class);

        clock.advance(Duration.ofMinutes(16));

        assertThatCode(() -> service.checkNotLocked("alice")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("usernames are tracked independently of one another")
    void perUsernameIsolation() {
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), Clock.systemUTC());

        service.recordFailure("alice");
        service.recordFailure("alice");

        assertThatThrownBy(() -> service.checkNotLocked("alice")).isInstanceOf(TooManyAttemptsException.class);
        assertThatCode(() -> service.checkNotLocked("bob")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("username matching ignores case and surrounding whitespace")
    void usernameNormalization() {
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), Clock.systemUTC());

        service.recordFailure("Alice");
        service.recordFailure("  alice  ");

        assertThatThrownBy(() -> service.checkNotLocked("ALICE"))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    @DisplayName("a null username doesn't throw — it's tracked as its own bucket")
    void nullUsernameHandledGracefully() {
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), Clock.systemUTC());

        assertThatCode(() -> {
            service.recordFailure(null);
            service.checkNotLocked(null);
        }).doesNotThrowAnyException();
    }
}
