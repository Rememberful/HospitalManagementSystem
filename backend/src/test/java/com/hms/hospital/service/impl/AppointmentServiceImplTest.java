package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.AppointmentCreateRequest;
import com.hms.hospital.dto.request.AppointmentUpdateRequest;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.AppointmentRepository;
import com.hms.hospital.repository.NurseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PhysicianRepository physicianRepository;
    @Mock
    private NurseRepository nurseRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointment = new Appointment(1L, 100000001L, 101, 1, "2008-04-24 10:00", "2008-04-24 11:00", "A", "OPD");
    }

    @Test
    @DisplayName("book saves the appointment when the caller supplies a PrepNurse explicitly")
    void booksWithExplicitPrepNurse() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 100000001L, 101, 1, "s", "e", "A", "OPD");
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.existsById(1L)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.book(request);

        assertThat(result.getPrepNurse()).isEqualTo(101);
        verify(nurseRepository, never()).findAll();
    }

    @Test
    @DisplayName("book auto-assigns a prep nurse from the roster when none is supplied")
    void autoAssignsPrepNurseWhenAbsent() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 100000001L, null, 1, "s", "e", "A", "OPD");
        Nurse onlyNurse = new Nurse(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.existsById(1L)).thenReturn(false);
        when(nurseRepository.findAll()).thenReturn(List.of(onlyNurse));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.book(request);

        assertThat(result.getPrepNurse()).isEqualTo(101);
    }

    @Test
    @DisplayName("book leaves PrepNurse null when none is supplied and no nurses exist")
    void leavesPrepNurseNullWhenNoNursesExist() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 100000001L, null, 1, "s", "e", "A", "OPD");
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.existsById(1L)).thenReturn(false);
        when(nurseRepository.findAll()).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.book(request);

        assertThat(result.getPrepNurse()).isNull();
    }

    @Test
    @DisplayName("book defaults VisitType to OPD when the caller omits it")
    void defaultsVisitTypeToOpd() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 100000001L, 101, 1, "s", "e", "A", null);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.existsById(1L)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.book(request);

        assertThat(result.getVisitType()).isEqualTo("OPD");
    }

    @Test
    @DisplayName("book rejects an unknown Patient SSN")
    void rejectsUnknownPatient() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 999L, 101, 1, "s", "e", "A", "OPD");
        when(patientRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.book(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Patient must reference");
    }

    @Test
    @DisplayName("book rejects an unknown Physician")
    void rejectsUnknownPhysician() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 100000001L, 101, 999, "s", "e", "A", "OPD");
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.book(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Physician must reference");
    }

    @Test
    @DisplayName("book rejects a duplicate AppointmentID")
    void rejectsDuplicateAppointmentId() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 100000001L, 101, 1, "s", "e", "A", "OPD");
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.book(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("update patches only the fields supplied, leaving the rest untouched")
    void updatePartialFields() {
        AppointmentUpdateRequest request = new AppointmentUpdateRequest(null, "2008-05-01 09:00", null, null, null);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.update(1L, request);

        assertThat(result.getStarto()).isEqualTo("2008-05-01 09:00");
        assertThat(result.getPhysician()).isEqualTo(1); // unchanged
        assertThat(result.getExaminationRoom()).isEqualTo("A"); // unchanged
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException for an unknown AppointmentID")
    void updateMissing() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.update(404L, new AppointmentUpdateRequest(null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findAll returns every appointment on file")
    void findAll() {
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));

        assertThat(appointmentService.findAll()).containsExactly(appointment);
    }

    @Test
    @DisplayName("getById returns the appointment when found")
    void getByIdFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThat(appointmentService.getById(1L)).isEqualTo(appointment);
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException for an unknown AppointmentID")
    void getByIdMissing() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
