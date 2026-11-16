package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.ProcedureCreateRequest;
import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.MedicalProcedureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class ProcedureServiceImplTest {

    @Mock
    private MedicalProcedureRepository procedureRepository;

    @InjectMocks
    private ProcedureServiceImpl procedureService;

    private MedicalProcedure procedure;

    @BeforeEach
    void setUp() {
        procedure = new MedicalProcedure(1, "Reverse Rhinopodoplasty", 1500.0);
    }

    @Test
    @DisplayName("create saves a new procedure when the Code is free")
    void createsProcedure() {
        ProcedureCreateRequest request = new ProcedureCreateRequest(1, "Reverse Rhinopodoplasty", 1500.0);
        when(procedureRepository.existsById(1)).thenReturn(false);
        when(procedureRepository.save(any(MedicalProcedure.class))).thenReturn(procedure);

        MedicalProcedure result = procedureService.create(request);

        assertThat(result.getName()).isEqualTo("Reverse Rhinopodoplasty");
    }

    @Test
    @DisplayName("create rejects a duplicate Code")
    void rejectsDuplicateCode() {
        ProcedureCreateRequest request = new ProcedureCreateRequest(1, "Reverse Rhinopodoplasty", 1500.0);
        when(procedureRepository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> procedureService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("getCost returns the stored cost")
    void getCost() {
        when(procedureRepository.findById(1)).thenReturn(Optional.of(procedure));

        assertThat(procedureService.getCost(1)).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("getCost throws ResourceNotFoundException for an unknown code")
    void getCostMissing() {
        when(procedureRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procedureService.getCost(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateCost saves the procedure with the new cost")
    void updateCost() {
        when(procedureRepository.findById(1)).thenReturn(Optional.of(procedure));
        when(procedureRepository.save(any(MedicalProcedure.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalProcedure result = procedureService.updateCost(1, 2000.0);

        assertThat(result.getCost()).isEqualTo(2000.0);
    }

    @Test
    @DisplayName("updateName saves the procedure with the new name")
    void updateName() {
        when(procedureRepository.findById(1)).thenReturn(Optional.of(procedure));
        when(procedureRepository.save(any(MedicalProcedure.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalProcedure result = procedureService.updateName(1, "Renamed Procedure");

        assertThat(result.getName()).isEqualTo("Renamed Procedure");
    }

    @Test
    @DisplayName("searchByName delegates to the case-insensitive contains finder")
    void searchByName() {
        when(procedureRepository.findByNameContainingIgnoreCase("rhino")).thenReturn(List.of(procedure));

        assertThat(procedureService.searchByName("rhino")).containsExactly(procedure);
    }

    @Test
    @DisplayName("findAll returns every procedure on file")
    void findAll() {
        when(procedureRepository.findAll()).thenReturn(List.of(procedure));

        assertThat(procedureService.findAll()).containsExactly(procedure);
    }
}
