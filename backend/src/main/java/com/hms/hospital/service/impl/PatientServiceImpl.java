package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.PatientCreateRequest;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.AppointmentRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PhysicianRepository physicianRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public Patient create(PatientCreateRequest request) {
        if (!physicianRepository.existsById(request.pcp())) {
            throw new BadRequestException("PCP must reference an existing Physician");
        }
        if (patientRepository.existsById(request.ssn())) {
            throw new BadRequestException("Patient with this SSN already exists");
        }
        Patient patient = new Patient(request.ssn(), request.name(), request.address(),
                request.phone(), request.insuranceID(), request.pcp());
        return patientRepository.save(patient);
    }

    @Override
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Override
    public Patient getBySsn(Long ssn) {
        return patientRepository.findById(ssn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient"));
    }

    @Override
    public List<Patient> getByPhysician(Integer physicianId) {
        Set<Long> ssns = appointmentRepository.findByPhysician(physicianId).stream()
                .map(Appointment::getPatient)
                .collect(Collectors.toSet());
        return patientRepository.findBySsnIn(List.copyOf(ssns));
    }

    @Override
    public Patient getByPhysicianAndSsn(Integer physicianId, Long ssn) {
        appointmentRepository.findByPhysicianAndPatient(physicianId, ssn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient"));
        return getBySsn(ssn);
    }

    @Override
    public Long getInsuranceId(Long ssn) {
        return getBySsn(ssn).getInsuranceId();
    }

    @Override
    public Patient updateAddress(Long ssn, String address) {
        Patient patient = getBySsn(ssn);
        patient.setAddress(address);
        return patientRepository.save(patient);
    }

    @Override
    public Patient updatePhone(Long ssn, String phone) {
        Patient patient = getBySsn(ssn);
        patient.setPhone(phone);
        return patientRepository.save(patient);
    }
}
