package com.hms.hospital.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.hospital.dto.response.UserResponse;
import com.hms.hospital.entity.AppUser;
import com.hms.hospital.entity.Medication;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.Room;
import com.hms.hospital.entity.Salary;
import com.hms.hospital.entity.Stay;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private PhysicianRepository physicianRepository;
    @Mock
    private NurseRepository nurseRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private MedicalProcedureRepository procedureRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private SalaryRepository salaryRepository;
    @Mock
    private StayRepository stayRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminServiceImpl adminService;

    private Physician physician;

    @BeforeEach
    void setUp() {
        // Real ObjectMapper — the merge logic is the thing under test, so it must actually run.
        ObjectMapper objectMapper = new ObjectMapper();
        adminService = new AdminServiceImpl(physicianRepository, nurseRepository, patientRepository,
                departmentRepository, procedureRepository, appointmentRepository, roomRepository,
                medicationRepository, salaryRepository, stayRepository, appUserRepository, objectMapper, passwordEncoder);
        physician = new Physician(1, "John Dorian", "Staff Internist", 111111111L);
    }

    @Test
    @DisplayName("get dispatches to the right repository based on the resource name")
    void getDispatchesByResource() {
        when(physicianRepository.findById(1)).thenReturn(Optional.of(physician));

        Object result = adminService.get("physicians", 1L);

        assertThat(result).isEqualTo(physician);
    }

    @Test
    @DisplayName("get throws ResourceNotFoundException when the id doesn't exist for that resource")
    void getMissingRecord() {
        when(physicianRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.get("physicians", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("get rejects a resource name outside the admin-manageable set (composite-key resources have their own controllers)")
    void getRejectsUnknownResource() {
        assertThatThrownBy(() -> adminService.get("blocks", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unknown or non-editable resource");
    }

    @Test
    @DisplayName("get dispatches medications, salaries, stays and users too")
    void getDispatchesNewlyAddedResources() {
        Medication medication = new Medication(1, "Ibuprofen", "Advil", "NSAID");
        Salary salary = new Salary(1, 9500.0, 0.04);
        Stay stay = new Stay(1, 100000001L, 101, "2030-01-01", null);
        AppUser user = new AppUser(1, "admin", "$2a$hashedvalue", "admin", "System Admin", null, null);
        when(medicationRepository.findById(1)).thenReturn(Optional.of(medication));
        when(salaryRepository.findById(1)).thenReturn(Optional.of(salary));
        when(stayRepository.findById(1)).thenReturn(Optional.of(stay));
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));

        assertThat(adminService.get("medications", 1L)).isEqualTo(medication);
        assertThat(adminService.get("salaries", 1L)).isEqualTo(salary);
        assertThat(adminService.get("stays", 1L)).isEqualTo(stay);
        assertThat(adminService.get("users", 1L)).isEqualTo(UserResponse.from(user));
    }

    @Test
    @DisplayName("update merges only the patched fields onto the existing record")
    void updateMergesPatch() {
        when(physicianRepository.findById(1)).thenReturn(Optional.of(physician));
        when(physicianRepository.save(any(Physician.class))).thenAnswer(inv -> inv.getArgument(0));

        Object result = adminService.update("physicians", 1L, Map.of("Position", "Attending Physician"));

        Physician updated = (Physician) result;
        assertThat(updated.getPosition()).isEqualTo("Attending Physician");
        assertThat(updated.getName()).isEqualTo("John Dorian"); // untouched fields survive the merge
        assertThat(updated.getEmployeeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("update on a room resource works through the same generic path")
    void updateRoomResource() {
        Room room = new Room(101, "Single", 1, 1, 0);
        when(roomRepository.findById(101)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        Object result = adminService.update("rooms", 101L, Map.of("Unavailable", 1));

        assertThat(((Room) result).getUnavailable()).isEqualTo(1);
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException before attempting to save a missing record")
    void updateMissingRecord() {
        when(physicianRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.update("physicians", 404L, Map.of("Name", "X")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(physicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes the record when it exists")
    void deleteExisting() {
        when(physicianRepository.findById(1)).thenReturn(Optional.of(physician));

        adminService.delete("physicians", 1L);

        verify(physicianRepository).delete(physician);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException instead of silently no-oping")
    void deleteMissing() {
        when(physicianRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.delete("physicians", 404L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(physicianRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete rejects a resource name outside the admin-manageable set")
    void deleteRejectsUnknownResource() {
        assertThatThrownBy(() -> adminService.delete("billing", 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("update never lets the resource's own id field be changed via the patch")
    void updateCannotChangeId() {
        when(physicianRepository.findById(1)).thenReturn(Optional.of(physician));
        when(physicianRepository.save(any(Physician.class))).thenAnswer(inv -> inv.getArgument(0));

        Object result = adminService.update("physicians", 1L, Map.of("EmployeeID", 999, "Name", "New Name"));

        Physician updated = (Physician) result;
        assertThat(updated.getEmployeeId()).isEqualTo(1);
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("update on a user account hashes a supplied password rather than storing it as plaintext")
    void updateHashesPassword() {
        AppUser user = new AppUser(1, "admin", "$2a$oldhash", "admin", "System Admin", null, null);
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPlaintextPw")).thenReturn("$2a$newhash");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        Object result = adminService.update("users", 1L, Map.of("password", "newPlaintextPw"));

        assertThat(((UserResponse) result).toString()).doesNotContain("newPlaintextPw").doesNotContain("newhash");
        verify(appUserRepository).save(argThat(saved -> "$2a$newhash".equals(saved.getPassword())));
    }

    @Test
    @DisplayName("update on a user account leaves the password untouched when the patch omits it")
    void updateWithoutPasswordLeavesItAlone() {
        AppUser user = new AppUser(1, "admin", "$2a$oldhash", "admin", "System Admin", null, null);
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.update("users", 1L, Map.of("displayName", "New Name"));

        verify(appUserRepository).save(argThat(saved -> "$2a$oldhash".equals(saved.getPassword())));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("update rejects a blank password rather than hashing an empty string")
    void updateRejectsBlankPasswordSilently() {
        AppUser user = new AppUser(1, "admin", "$2a$oldhash", "admin", "System Admin", null, null);
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.update("users", 1L, Map.of("password", "   "));

        verify(appUserRepository).save(argThat(saved -> "$2a$oldhash".equals(saved.getPassword())));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("update rejects a too-short password on a user account")
    void updateRejectsTooShortPassword() {
        AppUser user = new AppUser(1, "admin", "$2a$oldhash", "admin", "System Admin", null, null);
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.update("users", 1L, Map.of("password", "short")))
                .isInstanceOf(BadRequestException.class);
        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("update rejects an invalid role on a user account")
    void updateRejectsInvalidRole() {
        AppUser user = new AppUser(1, "admin", "$2a$oldhash", "admin", "System Admin", null, null);
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.update("users", 1L, Map.of("role", "superuser")))
                .isInstanceOf(BadRequestException.class);
        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("get on a user account never returns the password field")
    void getNeverReturnsPassword() {
        AppUser user = new AppUser(1, "admin", "$2a$hashedvalue", "admin", "System Admin", null, null);
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));

        Object result = adminService.get("users", 1L);

        assertThat(result.toString()).doesNotContain("hashedvalue");
    }
}
