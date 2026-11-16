package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.PrescriptionCreateRequest;
import com.hms.hospital.entity.Prescription;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.AppointmentRepository;
import com.hms.hospital.repository.MedicationRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.PrescriptionRepository;
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
class PrescriptionServiceImplTest {

    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private PhysicianRepository physicianRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private MedicationRepository medicationRepository;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private final PrescriptionCreateRequest validRequest =
            new PrescriptionCreateRequest(3, 100000001L, 1, "2030-01-01", null, "1 tablet twice daily");

    @Test
    @DisplayName("create saves a prescription when physician, patient and medication all exist")
    void createsPrescription() {
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(medicationRepository.existsById(1)).thenReturn(true);
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

        Prescription result = prescriptionService.create(validRequest);

        assertThat(result.getDose()).isEqualTo("1 tablet twice daily");
    }

    @Test
    @DisplayName("create links an optional Appointment when supplied and valid")
    void createsWithAppointmentLink() {
        PrescriptionCreateRequest request = new PrescriptionCreateRequest(3, 100000001L, 1, "2030-01-01", 5L, "dose");
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(medicationRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.existsById(5L)).thenReturn(true);
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

        Prescription result = prescriptionService.create(request);

        assertThat(result.getAppointment()).isEqualTo(5L);
    }

    @Test
    @DisplayName("create rejects an unknown physician")
    void rejectsUnknownPhysician() {
        when(physicianRepository.existsById(3)).thenReturn(false);

        assertThatThrownBy(() -> prescriptionService.create(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("physician must reference");
    }

    @Test
    @DisplayName("create rejects an unknown medication")
    void rejectsUnknownMedication() {
        when(physicianRepository.existsById(3)).thenReturn(true);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(medicationRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> prescriptionService.create(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("medication must reference");
    }

    @Test
    @DisplayName("findByPatient delegates to the repository finder")
    void findByPatient() {
        Prescription p = new Prescription("1", 3, 100000001L, 1, "2030-01-01", null, "dose");
        when(prescriptionRepository.findByPatient(100000001L)).thenReturn(List.of(p));

        assertThat(prescriptionService.findByPatient(100000001L)).containsExactly(p);
    }
}
