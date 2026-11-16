package com.hms.hospital.security;

import com.hms.hospital.entity.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-that-is-definitely-long-enough-for-hs256-signing-0123456789";

    private JwtService jwtService;
    private UserPrincipal doctor;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000);
        doctor = new UserPrincipal(new AppUser(2, "jdorian", "$2a$hash", "doctor", "John Dorian", null, 1));
    }

    @Test
    @DisplayName("a freshly issued token validates and yields the right username")
    void roundTrip() {
        String token = jwtService.issueToken(doctor);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jdorian");
        assertThat(jwtService.isValid(token, "jdorian")).isTrue();
    }

    @Test
    @DisplayName("a token is not valid for a different username than the one it was issued to")
    void wrongSubjectRejected() {
        String token = jwtService.issueToken(doctor);

        assertThat(jwtService.isValid(token, "someone-else")).isFalse();
    }

    @Test
    @DisplayName("a token with even one character altered is rejected (signature check)")
    void tamperedTokenRejected() {
        String token = jwtService.issueToken(doctor);
        char last = token.charAt(token.length() - 1);
        String tampered = token.substring(0, token.length() - 1) + (last == 'A' ? 'B' : 'A');

        assertThat(jwtService.extractUsername(tampered)).isNull();
        assertThat(jwtService.isValid(tampered, "jdorian")).isFalse();
    }

    @Test
    @DisplayName("a token whose payload was swapped for a different one (e.g. role escalation) is rejected")
    void payloadSwapRejected() {
        String token = jwtService.issueToken(doctor);
        String[] parts = token.split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"jdorian\",\"role\":\"admin\",\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8));
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThat(jwtService.extractUsername(forged)).isNull();
    }

    @Test
    @DisplayName("a token signed with a different secret is rejected")
    void foreignKeyRejected() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "a-completely-different-secret-key-value-also-long-enough-for-hs256!!".getBytes(StandardCharsets.UTF_8));
        String foreign = Jwts.builder().subject("jdorian").signWith(otherKey).compact();

        assertThat(jwtService.extractUsername(foreign)).isNull();
    }

    @Test
    @DisplayName("an expired token is rejected")
    void expiredTokenRejected() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expired = Jwts.builder()
                .subject("jdorian")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        assertThat(jwtService.extractUsername(expired)).isNull();
        assertThat(jwtService.isValid(expired, "jdorian")).isFalse();
    }

    @Test
    @DisplayName("an unsigned ('alg: none') token is rejected")
    void unsignedTokenRejected() {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"admin\",\"role\":\"admin\"}".getBytes(StandardCharsets.UTF_8));

        assertThat(jwtService.extractUsername(header + "." + payload + ".")).isNull();
    }

    @Test
    @DisplayName("garbage and empty input are rejected without throwing")
    void garbageRejected() {
        assertThat(jwtService.extractUsername("not-a-jwt")).isNull();
        assertThat(jwtService.extractUsername("")).isNull();
        assertThat(jwtService.isValid("not-a-jwt", "jdorian")).isFalse();
    }

    @Test
    @DisplayName("a near-zero lifetime yields a token that is immediately invalid")
    void zeroLifetimeTokenIsImmediatelyInvalid() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1);
        String token = shortLived.issueToken(doctor);
        Thread.sleep(20);

        assertThat(shortLived.isValid(token, "jdorian")).isFalse();
    }
}
