package com.hms.hospital.security;

import com.hms.hospital.exception.TooManyAttemptsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing: after {@code maxAttempts} consecutive failures for a username,
 * further sign-in attempts for it are refused for {@code lockout}, even with the right password.
 *
 * In-memory and per-instance on purpose — no extra infrastructure, and fine for one server. If
 * this is ever run as several instances behind a load balancer the counters would need to move
 * to a shared store (Redis, the database). One known trade-off of locking by username: someone
 * who knows a username can deliberately lock that person out for the lockout period. That is the
 * usual price of this defence and is bounded by the short lockout.
 */
@Service
public class LoginAttemptService {

    /** Upper bound on tracked usernames, so a flood of random usernames can't exhaust memory. */
    private static final int MAX_TRACKED = 10_000;

    private record State(int failures, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, State> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockout;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(@Value("${app.security.max-login-attempts:5}") int maxAttempts,
                               @Value("${app.security.lockout-minutes:15}") long lockoutMinutes) {
        this(maxAttempts, Duration.ofMinutes(lockoutMinutes), Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration lockout, Clock clock) {
        this.maxAttempts = maxAttempts;
        this.lockout = lockout;
        this.clock = clock;
    }

    /** Throws if this username is currently locked out. Call before checking the password. */
    public void checkNotLocked(String username) {
        String key = normalize(username);
        State state = attempts.get(key);
        if (state == null || state.lockedUntil() == null) return;

        Instant now = clock.instant();
        if (state.lockedUntil().isAfter(now)) {
            throw new TooManyAttemptsException(Duration.between(now, state.lockedUntil()).getSeconds());
        }
        attempts.remove(key, state);
    }

    public void recordFailure(String username) {
        if (attempts.size() >= MAX_TRACKED) evictStale();
        String key = normalize(username);
        attempts.merge(key, new State(1, null), (old, ignored) -> {
            int failures = old.failures() + 1;
            Instant lockedUntil = failures >= maxAttempts ? clock.instant().plus(lockout) : old.lockedUntil();
            return new State(failures, lockedUntil);
        });
        if (maxAttempts <= 1) {
            attempts.computeIfPresent(key, (k, s) -> new State(s.failures(), clock.instant().plus(lockout)));
        }
    }

    public void recordSuccess(String username) {
        attempts.remove(normalize(username));
    }

    private void evictStale() {
        Instant now = clock.instant();
        attempts.entrySet().removeIf(e -> e.getValue().lockedUntil() == null
                || !e.getValue().lockedUntil().isAfter(now));
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
