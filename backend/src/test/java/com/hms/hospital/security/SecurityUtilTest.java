package com.hms.hospital.security;

import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.AppUser;
import com.hms.hospital.repository.AppointmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private SecurityUtil securityUtil;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void signInAs(AppUser user) {
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private AppUser patient(long ssn) {
        return new AppUser(7, "jsmith", "h", "patient", "John Smith", ssn, null);
    }

    private AppUser staff(String role, int employeeId) {
        return new AppUser(2, "staff", "h", role, "Staff", null, employeeId);
    }

    @Test
    @DisplayName("isSelfPatient is true only for the patient's own SSN")
    void selfPatient() {
        signInAs(patient(100000001L));

        assertThat(securityUtil.isSelfPatient(100000001L)).isTrue();
        assertThat(securityUtil.isSelfPatient(100000002L)).isFalse();
    }

    @Test
    @DisplayName("isSelfPatient is false for a null SSN rather than throwing or matching")
    void selfPatientNull() {
        signInAs(patient(100000001L));

        assertThat(securityUtil.isSelfPatient(null)).isFalse();
    }

    @Test
    @DisplayName("a non-patient account (no SSN on file) is never 'self' for any patient")
    void staffIsNotSelfPatient() {
        signInAs(staff("doctor", 1));

        assertThat(securityUtil.isSelfPatient(100000001L)).isFalse();
    }

    @Test
    @DisplayName("isSelfDoctor is true only for the doctor's own EmployeeID")
    void selfDoctor() {
        signInAs(staff("doctor", 1));

        assertThat(securityUtil.isSelfDoctor(1)).isTrue();
        assertThat(securityUtil.isSelfDoctor(2)).isFalse();
        assertThat(securityUtil.isSelfDoctor(null)).isFalse();
    }

    @Test
    @DisplayName("isSelfNurse is true only for the nurse's own EmployeeID")
    void selfNurse() {
        signInAs(staff("nurse", 101));

        assertThat(securityUtil.isSelfNurse(101)).isTrue();
        assertThat(securityUtil.isSelfNurse(102)).isFalse();
    }

    @Test
    @DisplayName("with nobody signed in, every ownership check is false")
    void anonymousIsNobody() {
        assertThat(securityUtil.isSelfPatient(100000001L)).isFalse();
        assertThat(securityUtil.isSelfDoctor(1)).isFalse();
        assertThat(securityUtil.isSelfNurse(101)).isFalse();
        assertThat(securityUtil.ownsAppointment(1L)).isFalse();
    }

    @Test
    @DisplayName("a principal that isn't our UserPrincipal (e.g. an anonymous token) owns nothing")
    void foreignPrincipalOwnsNothing() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null));

        assertThat(securityUtil.isSelfPatient(100000001L)).isFalse();
    }

    @Test
    @DisplayName("ownsAppointment is true when the appointment's patient is the signed-in patient")
    void ownsOwnAppointment() {
        signInAs(patient(100000001L));
        lenient().when(appointmentRepository.findById(5L)).thenReturn(
                Optional.of(new Appointment(5L, 100000001L, 101, 1, "s", "e", "A", "OPD")));

        assertThat(securityUtil.ownsAppointment(5L)).isTrue();
    }

    @Test
    @DisplayName("ownsAppointment is false for someone else's appointment")
    void doesNotOwnOthersAppointment() {
        signInAs(patient(100000001L));
        lenient().when(appointmentRepository.findById(6L)).thenReturn(
                Optional.of(new Appointment(6L, 100000002L, 101, 1, "s", "e", "A", "OPD")));

        assertThat(securityUtil.ownsAppointment(6L)).isFalse();
    }

    @Test
    @DisplayName("ownsAppointment is false (not an error) for an appointment that doesn't exist")
    void nonexistentAppointment() {
        signInAs(patient(100000001L));
        lenient().when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(securityUtil.ownsAppointment(404L)).isFalse();
    }
}
