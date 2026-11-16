package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.PatientCreateRequest;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.AppointmentRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PhysicianRepository physicianRepository;
    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);
    }

    @Test
    @DisplayName("create saves a patient when the PCP references a real physician")
    void createsPatient() {
        PatientCreateRequest request = new PatientCreateRequest(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(patientRepository.existsById(100000001L)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient result = patientService.create(request);

        assertThat(result.getName()).isEqualTo("John Smith");
    }

    @Test
    @DisplayName("create rejects a PCP that isn't a real physician")
    void rejectsUnknownPcp() {
        PatientCreateRequest request = new PatientCreateRequest(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 999);
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PCP must reference");
    }

    @Test
    @DisplayName("create rejects a duplicate SSN")
    void rejectsDuplicateSsn() {
        PatientCreateRequest request = new PatientCreateRequest(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(patientRepository.existsById(100000001L)).thenReturn(true);

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("getBySsn returns the patient when found")
    void getBySsnFound() {
        when(patientRepository.findById(100000001L)).thenReturn(Optional.of(patient));

        assertThat(patientService.getBySsn(100000001L)).isEqualTo(patient);
    }

    @Test
    @DisplayName("getBySsn throws ResourceNotFoundException when missing")
    void getBySsnMissing() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getBySsn(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByPhysician resolves patients through that physician's appointments")
    void getByPhysician() {
        Appointment appt = new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD");
        when(appointmentRepository.findByPhysician(1)).thenReturn(List.of(appt));
        when(patientRepository.findBySsnIn(anyList())).thenReturn(List.of(patient));

        List<Patient> result = patientService.getByPhysician(1);

        assertThat(result).containsExactly(patient);
    }

    @Test
    @DisplayName("getByPhysicianAndSsn returns the patient only if that appointment link exists")
    void getByPhysicianAndSsnFound() {
        Appointment appt = new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD");
        when(appointmentRepository.findByPhysicianAndPatient(1, 100000001L)).thenReturn(Optional.of(appt));
        when(patientRepository.findById(100000001L)).thenReturn(Optional.of(patient));

        assertThat(patientService.getByPhysicianAndSsn(1, 100000001L)).isEqualTo(patient);
    }

    @Test
    @DisplayName("getByPhysicianAndSsn throws ResourceNotFoundException when no such appointment link exists")
    void getByPhysicianAndSsnNoLink() {
        when(appointmentRepository.findByPhysicianAndPatient(1, 999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getByPhysicianAndSsn(1, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getInsuranceId returns the patient's InsuranceID")
    void getInsuranceId() {
        when(patientRepository.findById(100000001L)).thenReturn(Optional.of(patient));

        assertThat(patientService.getInsuranceId(100000001L)).isEqualTo(68476213L);
    }

    @Test
    @DisplayName("updateAddress saves the patient with the new address")
    void updateAddress() {
        when(patientRepository.findById(100000001L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient result = patientService.updateAddress(100000001L, "1 New Street");

        assertThat(result.getAddress()).isEqualTo("1 New Street");
    }

    @Test
    @DisplayName("updatePhone saves the patient with the new phone number")
    void updatePhone() {
        when(patientRepository.findById(100000001L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient result = patientService.updatePhone(100000001L, "555-9999");

        assertThat(result.getPhone()).isEqualTo("555-9999");
    }

    @Test
    @DisplayName("findAll returns every patient on file")
    void findAll() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        assertThat(patientService.findAll()).containsExactly(patient);
    }
}
