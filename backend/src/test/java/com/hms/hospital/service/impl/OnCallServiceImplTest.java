package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.OnCallCreateRequest;
import com.hms.hospital.entity.OnCall;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.OnCallRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnCallServiceImplTest {

    @Mock
    private OnCallRepository onCallRepository;
    @Mock
    private NurseRepository nurseRepository;

    @InjectMocks
    private OnCallServiceImpl onCallService;

    @Test
    @DisplayName("create saves the on-call record when the nurse exists")
    void createsOnCallRecord() {
        OnCallCreateRequest request = new OnCallCreateRequest(101, 1, 1, "2030-01-01 08:00", "2030-01-01 20:00");
        when(nurseRepository.existsById(101)).thenReturn(true);
        when(onCallRepository.save(any(OnCall.class))).thenAnswer(inv -> inv.getArgument(0));

        OnCall result = onCallService.create(request);

        assertThat(result.getNurse()).isEqualTo(101);
        assertThat(result.getId()).isNotBlank();
    }

    @Test
    @DisplayName("create rejects an unknown nurse")
    void rejectsUnknownNurse() {
        OnCallCreateRequest request = new OnCallCreateRequest(999, 1, 1, "s", "e");
        when(nurseRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> onCallService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException for an unknown id")
    void deleteMissing() {
        when(onCallRepository.existsById("nope")).thenReturn(false);

        assertThatThrownBy(() -> onCallService.delete("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
