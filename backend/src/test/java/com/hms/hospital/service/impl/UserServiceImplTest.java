package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.UserCreateRequest;
import com.hms.hospital.dto.response.UserResponse;
import com.hms.hospital.entity.AppUser;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.AppUserRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private PhysicianRepository physicianRepository;
    @Mock private NurseRepository nurseRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("create saves an admin account without needing an ssn or employeeId")
    void createsAdminAccount() {
        UserCreateRequest request = new UserCreateRequest(2, "newadmin", "pw", "admin", "New Admin", null, null);
        when(appUserRepository.existsById(2)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("$2a$hashed");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.create(request);

        assertThat(result.role()).isEqualTo("admin");
    }

    @Test
    @DisplayName("create links a doctor account to an existing Physician via employeeId")
    void createsDoctorAccount() {
        UserCreateRequest request = new UserCreateRequest(5, "drwho", "pw", "doctor", "Dr Who", null, 1);
        when(appUserRepository.existsById(5)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("drwho")).thenReturn(false);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.create(request);

        assertThat(result.employeeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("create rejects a doctor account whose employeeId isn't a real physician")
    void rejectsDoctorAccountWithUnknownPhysician() {
        UserCreateRequest request = new UserCreateRequest(5, "drwho", "pw", "doctor", "Dr Who", null, 999);
        when(appUserRepository.existsById(5)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("drwho")).thenReturn(false);
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("employeeId must reference an existing Physician");
    }

    @Test
    @DisplayName("create links a nurse account via employeeId")
    void createsNurseAccount() {
        UserCreateRequest request = new UserCreateRequest(6, "nursejoy", "pw", "nurse", "Nurse Joy", null, 101);
        when(appUserRepository.existsById(6)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("nursejoy")).thenReturn(false);
        when(nurseRepository.existsById(101)).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(userService.create(request).employeeId()).isEqualTo(101);
    }

    @Test
    @DisplayName("create links a patient account via ssn")
    void createsPatientAccount() {
        UserCreateRequest request = new UserCreateRequest(7, "jsmith2", "pw", "patient", "John Smith", 100000001L, null);
        when(appUserRepository.existsById(7)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("jsmith2")).thenReturn(false);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(userService.create(request).ssn()).isEqualTo(100000001L);
    }

    @Test
    @DisplayName("create rejects a patient account whose ssn isn't a real patient")
    void rejectsPatientAccountWithUnknownSsn() {
        UserCreateRequest request = new UserCreateRequest(7, "ghost", "pw", "patient", "Ghost", 999L, null);
        when(appUserRepository.existsById(7)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("ghost")).thenReturn(false);
        when(patientRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create rejects an invalid role")
    void rejectsInvalidRole() {
        UserCreateRequest request = new UserCreateRequest(8, "x", "pw", "superuser", "X", null, null);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("role must be one of");
    }

    @Test
    @DisplayName("create rejects a duplicate id")
    void rejectsDuplicateId() {
        UserCreateRequest request = new UserCreateRequest(1, "x", "pw", "admin", "X", null, null);
        when(appUserRepository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("create rejects a username that's already taken (the repository check is case-insensitive)")
    void rejectsDuplicateUsername() {
        UserCreateRequest request = new UserCreateRequest(9, "Admin", "pw", "admin", "X", null, null);
        when(appUserRepository.existsById(9)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("Admin")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("create stores a BCrypt-encoded password, never the plaintext")
    void storesHashedPasswordOnly() {
        UserCreateRequest request = new UserCreateRequest(2, "newadmin", "plain-secret", "admin", "New Admin", null, null);
        when(appUserRepository.existsById(2)).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase(any())).thenReturn(false);
        when(passwordEncoder.encode("plain-secret")).thenReturn("$2a$10$encodedvalue");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.create(request);

        verify(appUserRepository).save(argThat(saved ->
                "$2a$10$encodedvalue".equals(saved.getPassword()) && !"plain-secret".equals(saved.getPassword())));
    }

    @Test
    @DisplayName("the response type has no password component at all, so it can't be serialized by accident")
    void responseTypeHasNoPasswordComponent() {
        assertThat(UserResponse.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("password");
    }

    @Test
    @DisplayName("findAll returns every account on file, mapped away from the raw entity")
    void findAll() {
        AppUser admin = new AppUser(1, "admin", "$2a$hash", "admin", "System Admin", null, null);
        when(appUserRepository.findAll()).thenReturn(List.of(admin));

        assertThat(userService.findAll()).containsExactly(UserResponse.from(admin));
    }
}
