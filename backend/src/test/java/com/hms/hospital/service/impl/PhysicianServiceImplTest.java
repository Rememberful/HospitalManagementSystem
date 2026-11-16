package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.PhysicianCreateRequest;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.PhysicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysicianServiceImplTest {

    @Mock
    private PhysicianRepository physicianRepository;

    @InjectMocks
    private PhysicianServiceImpl physicianService;

    private Physician sample;

    @BeforeEach
    void setUp() {
        sample = new Physician(1, "John Dorian", "Staff Internist", 111111111L);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves a new physician when the EmployeeID is free")
        void createsPhysician() {
            PhysicianCreateRequest request = new PhysicianCreateRequest(1, "John Dorian", "Staff Internist", 111111111L);
            when(physicianRepository.existsById(1)).thenReturn(false);
            when(physicianRepository.save(any(Physician.class))).thenReturn(sample);

            Physician result = physicianService.create(request);

            assertThat(result.getEmployeeId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("John Dorian");
            verify(physicianRepository).save(any(Physician.class));
        }

        @Test
        @DisplayName("rejects a duplicate EmployeeID")
        void rejectsDuplicateEmployeeId() {
            PhysicianCreateRequest request = new PhysicianCreateRequest(1, "John Dorian", "Staff Internist", 111111111L);
            when(physicianRepository.existsById(1)).thenReturn(true);

            assertThatThrownBy(() -> physicianService.create(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
            verify(physicianRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getByEmployeeId")
    class GetByEmployeeId {

        @Test
        @DisplayName("returns the physician when found")
        void returnsPhysician() {
            when(physicianRepository.findById(1)).thenReturn(Optional.of(sample));

            Physician result = physicianService.getByEmployeeId(1);

            assertThat(result).isEqualTo(sample);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when missing")
        void throwsWhenMissing() {
            when(physicianRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> physicianService.getByEmployeeId(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Physician not found");
        }
    }

    @Test
    @DisplayName("getByName delegates to the case-insensitive repository finder")
    void getByName() {
        when(physicianRepository.findByNameIgnoreCase("john dorian")).thenReturn(Optional.of(sample));

        Physician result = physicianService.getByName("john dorian");

        assertThat(result).isEqualTo(sample);
    }

    @Test
    @DisplayName("getByName throws ResourceNotFoundException when no match")
    void getByNameNotFound() {
        when(physicianRepository.findByNameIgnoreCase("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> physicianService.getByName("nobody"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByPosition returns every physician holding that position")
    void getByPosition() {
        when(physicianRepository.findByPositionIgnoreCase("Staff Internist")).thenReturn(List.of(sample));

        List<Physician> result = physicianService.getByPosition("Staff Internist");

        assertThat(result).containsExactly(sample);
    }

    @Test
    @DisplayName("updateName saves the physician with the new name")
    void updateName() {
        when(physicianRepository.findById(1)).thenReturn(Optional.of(sample));
        when(physicianRepository.save(any(Physician.class))).thenAnswer(inv -> inv.getArgument(0));

        Physician result = physicianService.updateName(1, "Perry Cox");

        assertThat(result.getName()).isEqualTo("Perry Cox");
        verify(physicianRepository).save(sample);
    }

    @Test
    @DisplayName("updateSsn saves the physician with the new SSN")
    void updateSsn() {
        when(physicianRepository.findById(1)).thenReturn(Optional.of(sample));
        when(physicianRepository.save(any(Physician.class))).thenAnswer(inv -> inv.getArgument(0));

        Physician result = physicianService.updateSsn(1, 999999999L);

        assertThat(result.getSsn()).isEqualTo(999999999L);
    }

    @Test
    @DisplayName("updatePosition saves the physician with the new position")
    void updatePosition() {
        when(physicianRepository.findById(1)).thenReturn(Optional.of(sample));
        when(physicianRepository.save(any(Physician.class))).thenAnswer(inv -> inv.getArgument(0));

        Physician result = physicianService.updatePosition(1, "Attending Physician");

        assertThat(result.getPosition()).isEqualTo("Attending Physician");
    }

    @Test
    @DisplayName("updateName throws ResourceNotFoundException for an unknown EmployeeID")
    void updateNameNotFound() {
        when(physicianRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> physicianService.updateName(404, "X"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findAll returns every physician on file")
    void findAll() {
        when(physicianRepository.findAll()).thenReturn(List.of(sample));

        assertThat(physicianService.findAll()).hasSize(1);
    }
}
