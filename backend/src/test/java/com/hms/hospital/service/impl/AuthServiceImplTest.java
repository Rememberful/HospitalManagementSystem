package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.LoginResponse;
import com.hms.hospital.entity.AppUser;
import com.hms.hospital.exception.UnauthorizedException;
import com.hms.hospital.repository.AppUserRepository;
import com.hms.hospital.security.JwtService;
import com.hms.hospital.security.LoginAttemptService;
import com.hms.hospital.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private LoginAttemptService loginAttempts;

    @InjectMocks
    private AuthServiceImpl authService;

    private final AppUser admin = new AppUser(1, "admin", "$2a$hash", "admin", "System Admin", null, null);

    @Test
    @DisplayName("login verifies against the stored hash and returns a token plus the user, without the password")
    void loginSuccess() {
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin123", "$2a$hash")).thenReturn(true);
        when(jwtService.issueToken(any(UserPrincipal.class))).thenReturn("signed.jwt.token");

        LoginResponse result = authService.login("admin", "admin123");

        assertThat(result.token()).isEqualTo("signed.jwt.token");
        assertThat(result.user().username()).isEqualTo("admin");
        assertThat(result.user().role()).isEqualTo("admin");
        verify(loginAttempts).recordSuccess("admin");
    }

    @Test
    @DisplayName("login surfaces the SSN for a patient account")
    void loginPatientAccountIncludesSsn() {
        AppUser patientUser = new AppUser(3, "jsmith", "$2a$hash", "patient", "John Smith", 100000001L, null);
        when(appUserRepository.findByUsername("jsmith")).thenReturn(Optional.of(patientUser));
        when(passwordEncoder.matches("patient123", "$2a$hash")).thenReturn(true);
        when(jwtService.issueToken(any(UserPrincipal.class))).thenReturn("t");

        LoginResponse result = authService.login("jsmith", "patient123");

        assertThat(result.user().ssn()).isEqualTo(100000001L);
        assertThat(result.user().employeeId()).isNull();
    }

    @Test
    @DisplayName("login checks the lockout before attempting the password check")
    void checksLockoutFirst() {
        doThrow(new com.hms.hospital.exception.TooManyAttemptsException(60)).when(loginAttempts).checkNotLocked("admin");

        assertThatThrownBy(() -> authService.login("admin", "admin123"))
                .isInstanceOf(com.hms.hospital.exception.TooManyAttemptsException.class);
        verifyNoInteractions(appUserRepository);
    }

    @Test
    @DisplayName("login rejects a wrong password, records the failure, and never issues a token")
    void wrongPassword() {
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("admin", "wrong"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
        verify(jwtService, never()).issueToken(any());
        verify(loginAttempts).recordFailure("admin");
    }

    @Test
    @DisplayName("login gives the same error for an unknown username as for a wrong password (no user enumeration)")
    void unknownUsernameLooksIdenticalToWrongPassword() {
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login("ghost", "whatever"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
        verify(jwtService, never()).issueToken(any());
    }

    @Test
    @DisplayName("an unknown username still runs a password comparison (timing-attack mitigation)")
    void unknownUsernameStillChecksAPassword() {
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("$2a$dummy");
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        try {
            authService.login("ghost", "whatever");
        } catch (UnauthorizedException ignored) { /* expected */ }

        verify(passwordEncoder).matches(eq("whatever"), any());
    }
}
