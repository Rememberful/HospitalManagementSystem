package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.MedicationCreateRequest;
import com.hms.hospital.entity.Medication;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.MedicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationServiceImpl medicationService;

    @Test
    @DisplayName("create saves a new medication when the Code is free")
    void createsMedication() {
        MedicationCreateRequest request = new MedicationCreateRequest(1, "Ibuprofen", "Advil", "NSAID pain reliever");
        when(medicationRepository.existsById(1)).thenReturn(false);
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        Medication result = medicationService.create(request);

        assertThat(result.getName()).isEqualTo("Ibuprofen");
    }

    @Test
    @DisplayName("create rejects a duplicate Code")
    void rejectsDuplicateCode() {
        MedicationCreateRequest request = new MedicationCreateRequest(1, "Ibuprofen", "Advil", "NSAID");
        when(medicationRepository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> medicationService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("findAll returns every medication on file")
    void findAll() {
        Medication med = new Medication(1, "Ibuprofen", "Advil", "NSAID");
        when(medicationRepository.findAll()).thenReturn(List.of(med));

        assertThat(medicationService.findAll()).containsExactly(med);
    }
}
