package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.PrescriptionCreateRequest;
import com.hms.hospital.entity.Prescription;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.AppointmentRepository;
import com.hms.hospital.repository.MedicationRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.PrescriptionRepository;
import com.hms.hospital.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PhysicianRepository physicianRepository;
    private final PatientRepository patientRepository;
    private final MedicationRepository medicationRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public Prescription create(PrescriptionCreateRequest request) {
        if (!physicianRepository.existsById(request.physician())) {
            throw new BadRequestException("physician must reference an existing Physician");
        }
        if (!patientRepository.existsById(request.patient())) {
            throw new BadRequestException("patient must reference an existing Patient");
        }
        if (!medicationRepository.existsById(request.medication())) {
            throw new BadRequestException("medication must reference an existing Medication");
        }
        if (request.appointment() != null && !appointmentRepository.existsById(request.appointment())) {
            throw new BadRequestException("appointment must reference an existing Appointment");
        }
        Prescription prescription = new Prescription(UUID.randomUUID().toString(), request.physician(),
                request.patient(), request.medication(), request.date(), request.appointment(), request.dose());
        return prescriptionRepository.save(prescription);
    }

    @Override
    public List<Prescription> findByPatient(Long patient) {
        return prescriptionRepository.findByPatient(patient);
    }

    @Override
    public List<Prescription> findAll() {
        return prescriptionRepository.findAll();
    }
}
