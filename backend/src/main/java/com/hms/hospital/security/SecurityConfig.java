package com.hms.hospital.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * The whole app's authorization policy lives here, in one place, so it can be read and audited
 * as a unit. It handles "which role(s) may call this endpoint shape at all" — per-record
 * ownership (e.g. "is this patient looking at their OWN dashboard") is enforced separately with
 * @PreAuthorize/@PostAuthorize + SecurityUtil on the handful of endpoints where that matters; see
 * the controllers for patient/doctor/nurse dashboards and patient self-service.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // stateless bearer-token API — no cookies to forge
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // ── Admin-only: full CRUD surfaces and anything exposing sensitive
                        //    full-roster data (staff SSNs, all patients, pay, credentials) ──
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/salary/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/physician/post").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/physician/update/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/update/position/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/nurse").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/nurse/registered/**", "/api/nurse/ssn/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/patient").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/department").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/department/update/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/procedure").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/procedure/cost/**", "/api/procedure/name/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/medication").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/room").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/room/availability/**").hasRole("ADMIN")
                        .requestMatchers("/api/block/**").hasRole("ADMIN")
                        .requestMatchers("/api/on_call/**").hasRole("ADMIN")
                        .requestMatchers("/api/duty_shift/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/affiliated_with/post").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/trained_in").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/billing").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/billing/*/pay").hasRole("ADMIN")
                        // Admissions: front-desk/ward work in most hospitals, so nurses too
                        .requestMatchers("/api/stay/**").hasAnyRole("ADMIN", "NURSE")

                        // Full-roster listings. Both the bare path and its trailing-slash twin are
                        // listed on purpose: Spring 6 treats "/x" and "/x/" as different URLs, and
                        // several controllers map both — omitting one would leave a bypass.
                        .requestMatchers(HttpMethod.GET, "/api/billing", "/api/billing/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/appointment", "/api/appointment/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/patient", "/api/patient/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/nurse", "/api/nurse/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/prescribe", "/api/prescribe/").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/undergoes", "/api/undergoes/").hasAnyRole("ADMIN", "DOCTOR")
                        // Plain fallback list routes: everything except the shared catalog data
                        // (physicians, procedures, rooms, medications, departments) is operational
                        // or personal data, so admin-only.
                        .requestMatchers(HttpMethod.GET,
                                "/patients", "/nurses", "/users", "/salaries", "/billing", "/appointments",
                                "/prescribes", "/stays", "/undergoes", "/on_call", "/duty_shifts", "/blocks",
                                "/affiliated_with", "/trained_in").hasRole("ADMIN")

                        // ── Doctor + admin: clinical actions and the doctor's own views ──
                        .requestMatchers(HttpMethod.POST, "/api/prescribe").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/undergoes").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers("/api/doctor/**").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers("/api/patient/physician/**").hasAnyRole("ADMIN", "DOCTOR")

                        // ── Nurse + admin ──
                        .requestMatchers("/api/nurse/*/dashboard").hasAnyRole("ADMIN", "NURSE")

                        // ── Patient + admin: self-service (ownership enforced in the
                        //    controllers themselves via @PreAuthorize + SecurityUtil) ──
                        .requestMatchers("/api/patient/*/dashboard").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/appointment/**").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/patient/appointment/**").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/patient/address/**", "/api/patient/phone/**").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/patient/insurance/**").hasAnyRole("ADMIN", "PATIENT")

                        // ── Everything else: general catalog/reference lookups (physicians,
                        //    procedures, rooms, medications, departments, ...) needed across
                        //    booking forms and dashboards — any authenticated role ──
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
