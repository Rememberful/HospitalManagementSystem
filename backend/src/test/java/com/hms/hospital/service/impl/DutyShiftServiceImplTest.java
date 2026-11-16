package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.DutyShiftCreateRequest;
import com.hms.hospital.entity.DutyShift;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.DutyShiftRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.PhysicianRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DutyShiftServiceImplTest {

    @Mock private DutyShiftRepository dutyShiftRepository;
    @Mock private PhysicianRepository physicianRepository;
    @Mock private NurseRepository nurseRepository;

    @InjectMocks
    private DutyShiftServiceImpl dutyShiftService;

    @Test
    @DisplayName("create saves a physician shift and defaults shiftType to Regular")
    void createsPhysicianShift() {
        DutyShiftCreateRequest request = new DutyShiftCreateRequest("physician", 3, "2030-01-01", "08:00", "16:00", null);
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(dutyShiftRepository.save(any(DutyShift.class))).thenAnswer(inv -> inv.getArgument(0));

        DutyShift result = dutyShiftService.create(request);

        assertThat(result.getShiftType()).isEqualTo("Regular");
        assertThat(result.getPersonType()).isEqualTo("physician");
    }

    @Test
    @DisplayName("create saves a nurse shift, matched case-insensitively")
    void createsNurseShiftCaseInsensitive() {
        DutyShiftCreateRequest request = new DutyShiftCreateRequest("Nurse", 101, "2030-01-01", "08:00", "16:00", "Night");
        when(nurseRepository.existsById(101)).thenReturn(true);
        when(dutyShiftRepository.save(any(DutyShift.class))).thenAnswer(inv -> inv.getArgument(0));

        DutyShift result = dutyShiftService.create(request);

        assertThat(result.getPersonType()).isEqualTo("nurse");
        assertThat(result.getShiftType()).isEqualTo("Night");
    }

    @Test
    @DisplayName("create rejects a personType other than physician/nurse")
    void rejectsInvalidPersonType() {
        DutyShiftCreateRequest request = new DutyShiftCreateRequest("janitor", 1, "2030-01-01", "08:00", "16:00", null);

        assertThatThrownBy(() -> dutyShiftService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("personType must be");
    }

    @Test
    @DisplayName("create rejects a personId that doesn't reference a real physician")
    void rejectsUnknownPhysician() {
        DutyShiftCreateRequest request = new DutyShiftCreateRequest("physician", 999, "2030-01-01", "08:00", "16:00", null);
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> dutyShiftService.create(request))
                .isInstanceOf(BadRequestException.class);
    }
}
