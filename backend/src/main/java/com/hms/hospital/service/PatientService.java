package com.hms.hospital.service;

import com.hms.hospital.dto.request.PatientCreateRequest;
import com.hms.hospital.entity.Patient;

import java.util.List;

public interface PatientService {
    Patient create(PatientCreateRequest request);
    List<Patient> findAll();
    Patient getBySsn(Long ssn);
    List<Patient> getByPhysician(Integer physicianId);
    Patient getByPhysicianAndSsn(Integer physicianId, Long ssn);
    Long getInsuranceId(Long ssn);
    Patient updateAddress(Long ssn, String address);
    Patient updatePhone(Long ssn, String phone);
}
