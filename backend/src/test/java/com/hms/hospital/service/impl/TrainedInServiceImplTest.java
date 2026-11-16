package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.TrainedInWithProcedureView;
import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.TrainedIn;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.MedicalProcedureRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.TrainedInRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainedInServiceImplTest {

    @Mock
    private TrainedInRepository trainedInRepository;
    @Mock
    private PhysicianRepository physicianRepository;
    @Mock
    private MedicalProcedureRepository procedureRepository;

    @InjectMocks
    private TrainedInServiceImpl trainedInService;

    @Test
    @DisplayName("create builds the composite id from Physician and Treatment")
    void createsCertification() {
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(procedureRepository.existsById(1)).thenReturn(true);
        when(trainedInRepository.existsById("3-1")).thenReturn(false);
        when(trainedInRepository.save(any(TrainedIn.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainedIn result = trainedInService.create(3, 1, "2008-01-01", "2008-12-31");

        assertThat(result.getId()).isEqualTo("3-1");
    }

    @Test
    @DisplayName("create rejects missing required fields")
    void rejectsMissingFields() {
        assertThatThrownBy(() -> trainedInService.create(null, 1, "2008-01-01", "2008-12-31"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> trainedInService.create(3, 1, null, "2008-12-31"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create rejects an unknown Physician")
    void rejectsUnknownPhysician() {
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> trainedInService.create(999, 1, "2008-01-01", "2008-12-31"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Physician must reference");
    }

    @Test
    @DisplayName("create rejects an unknown Treatment/Procedure")
    void rejectsUnknownProcedure() {
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(procedureRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> trainedInService.create(3, 999, "2008-01-01", "2008-12-31"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Treatment must reference");
    }

    @Test
    @DisplayName("create rejects a duplicate certification")
    void rejectsDuplicateCertification() {
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(procedureRepository.existsById(1)).thenReturn(true);
        when(trainedInRepository.existsById("3-1")).thenReturn(true);

        assertThatThrownBy(() -> trainedInService.create(3, 1, "2008-01-01", "2008-12-31"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already certified");
    }

    @Test
    @DisplayName("treatmentsForPhysician enriches each certification with its procedure")
    void treatmentsForPhysician() {
        TrainedIn cert = new TrainedIn("3-1", 3, 1, "2008-01-01", "2008-12-31");
        MedicalProcedure proc = new MedicalProcedure(1, "Reverse Rhinopodoplasty", 1500.0);
        when(trainedInRepository.findByPhysician(3)).thenReturn(List.of(cert));
        when(procedureRepository.findById(1)).thenReturn(Optional.of(proc));

        List<TrainedInWithProcedureView> result = trainedInService.treatmentsForPhysician(3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).procedure()).isEqualTo(proc);
    }

    @Test
    @DisplayName("treatmentsForPhysician tolerates a dangling procedure reference")
    void treatmentsForPhysicianDanglingProcedure() {
        TrainedIn cert = new TrainedIn("3-99", 3, 99, "2008-01-01", "2008-12-31");
        when(trainedInRepository.findByPhysician(3)).thenReturn(List.of(cert));
        when(procedureRepository.findById(99)).thenReturn(Optional.empty());

        List<TrainedInWithProcedureView> result = trainedInService.treatmentsForPhysician(3);

        assertThat(result.get(0).procedure()).isNull();
    }

    @Test
    @DisplayName("physiciansForProcedure resolves certifications to full Physician records")
    void physiciansForProcedure() {
        Physician physician = new Physician(3, "Christopher Turk", "Surgeon", 333333333L);
        TrainedIn cert = new TrainedIn("3-1", 3, 1, "2008-01-01", "2008-12-31");
        when(trainedInRepository.findByTreatment(1)).thenReturn(List.of(cert));
        when(physicianRepository.findAllById(Set.of(3))).thenReturn(List.of(physician));

        assertThat(trainedInService.physiciansForProcedure(1)).containsExactly(physician);
    }

    @Test
    @DisplayName("findAll returns every certification on file")
    void findAll() {
        TrainedIn cert = new TrainedIn("3-1", 3, 1, "2008-01-01", "2008-12-31");
        when(trainedInRepository.findAll()).thenReturn(List.of(cert));

        assertThat(trainedInService.findAll()).containsExactly(cert);
    }
}
