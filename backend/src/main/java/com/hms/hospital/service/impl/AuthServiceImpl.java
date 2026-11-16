package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.LoginResponse;
import com.hms.hospital.dto.response.UserResponse;
import com.hms.hospital.entity.AppUser;
import com.hms.hospital.exception.UnauthorizedException;
import com.hms.hospital.repository.AppUserRepository;
import com.hms.hospital.security.JwtService;
import com.hms.hospital.security.LoginAttemptService;
import com.hms.hospital.security.UserPrincipal;
import com.hms.hospital.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttempts;

    /** Compared against when the username doesn't exist, so that path costs the same as a real check. */
    private volatile String timingDummyHash;

    @Override
    public LoginResponse login(String username, String password) {
        loginAttempts.checkNotLocked(username);

        AppUser user = appUserRepository.findByUsername(username).orElse(null);

        // Always run one password comparison, real or dummy. Otherwise "unknown user" returns in
        // microseconds while "wrong password" takes a full BCrypt round, and the difference in
        // response time tells an attacker which usernames exist.
        String hashToCheck = user != null ? user.getPassword() : dummyHash();
        boolean passwordOk = passwordEncoder.matches(password, hashToCheck);

        if (user == null || !passwordOk) {
            loginAttempts.recordFailure(username);
            throw new UnauthorizedException("Invalid credentials");
        }

        loginAttempts.recordSuccess(username);
        String token = jwtService.issueToken(new UserPrincipal(user));
        return new LoginResponse(token, UserResponse.from(user));
    }

    private String dummyHash() {
        String hash = timingDummyHash;
        if (hash == null) {
            hash = passwordEncoder.encode("timing-equalizer-not-a-real-password");
            timingDummyHash = hash;
        }
        return hash;
    }
}
