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
import com.hms.hospital.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;

    @Override
    public Appointment book(AppointmentCreateRequest request) {
        if (!patientRepository.existsById(request.patient())) {
            throw new BadRequestException("Patient must reference an existing Patient SSN");
        }
        if (!physicianRepository.existsById(request.physician())) {
            throw new BadRequestException("Physician must reference an existing Physician");
        }
        if (appointmentRepository.existsById(request.appointmentID())) {
            throw new BadRequestException("Appointment with this AppointmentID already exists");
        }

        Integer prepNurse = request.prepNurse();
        if (prepNurse == null) {
            // Auto-assign a prep nurse at random from the nurses currently on file, mirroring
            // the original mock API's behaviour, whenever the caller doesn't supply one.
            List<Nurse> nurses = nurseRepository.findAll();
            if (!nurses.isEmpty()) {
                prepNurse = nurses.get(ThreadLocalRandom.current().nextInt(nurses.size())).getEmployeeId();
            }
        }

        Appointment appointment = new Appointment(
                request.appointmentID(), request.patient(), prepNurse, request.physician(),
                request.starto(), request.endo(), request.examinationRoom(),
                request.visitType() == null ? "OPD" : request.visitType());
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment getById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment"));
    }

    @Override
    public Appointment update(Long appointmentId, AppointmentUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment"));
        if (request.physician() != null) appointment.setPhysician(request.physician());
        if (request.starto() != null) appointment.setStarto(request.starto());
        if (request.endo() != null) appointment.setEndo(request.endo());
        if (request.examinationRoom() != null) appointment.setExaminationRoom(request.examinationRoom());
        if (request.visitType() != null) appointment.setVisitType(request.visitType());
        return appointmentRepository.save(appointment);
    }
}
