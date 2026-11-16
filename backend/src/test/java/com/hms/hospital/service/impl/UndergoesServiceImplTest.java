package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.UndergoesCreateRequest;
import com.hms.hospital.entity.Undergoes;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.MedicalProcedureRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.StayRepository;
import com.hms.hospital.repository.UndergoesRepository;
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
class UndergoesServiceImplTest {

    @Mock private UndergoesRepository undergoesRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private MedicalProcedureRepository procedureRepository;
    @Mock private PhysicianRepository physicianRepository;
    @Mock private NurseRepository nurseRepository;
    @Mock private StayRepository stayRepository;

    @InjectMocks
    private UndergoesServiceImpl undergoesService;

    private final UndergoesCreateRequest validRequest =
            new UndergoesCreateRequest(100000001L, 6, 3215, "2030-01-01", 3, 101);

    @Test
    @DisplayName("record saves the procedure when every reference is valid")
    void recordsProcedure() {
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(procedureRepository.existsById(6)).thenReturn(true);
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(stayRepository.existsById(3215)).thenReturn(true);
        when(nurseRepository.existsById(101)).thenReturn(true);
        when(undergoesRepository.save(any(Undergoes.class))).thenAnswer(inv -> inv.getArgument(0));

        Undergoes result = undergoesService.record(validRequest);

        assertThat(result.getPhysician()).isEqualTo(3);
    }

    @Test
    @DisplayName("record allows a null Stay and null AssistingNurse (outpatient procedure)")
    void recordsWithoutStayOrNurse() {
        UndergoesCreateRequest request = new UndergoesCreateRequest(100000001L, 6, null, "2030-01-01", 3, null);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(procedureRepository.existsById(6)).thenReturn(true);
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(undergoesRepository.save(any(Undergoes.class))).thenAnswer(inv -> inv.getArgument(0));

        Undergoes result = undergoesService.record(request);

        assertThat(result.getStay()).isNull();
        assertThat(result.getAssistingNurse()).isNull();
    }

    @Test
    @DisplayName("record rejects an unknown patient")
    void rejectsUnknownPatient() {
        when(patientRepository.existsById(100000001L)).thenReturn(false);

        assertThatThrownBy(() -> undergoesService.record(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("patient must reference");
    }

    @Test
    @DisplayName("record rejects an unknown procedure code")
    void rejectsUnknownProcedure() {
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(procedureRepository.existsById(6)).thenReturn(false);

        assertThatThrownBy(() -> undergoesService.record(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("procedures must reference");
    }

    @Test
    @DisplayName("record rejects an unknown stay reference when one is supplied")
    void rejectsUnknownStay() {
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(procedureRepository.existsById(6)).thenReturn(true);
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(stayRepository.existsById(3215)).thenReturn(false);

        assertThatThrownBy(() -> undergoesService.record(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("stay must reference");
    }
}
