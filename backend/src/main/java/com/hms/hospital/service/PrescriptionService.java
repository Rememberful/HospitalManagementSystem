package com.hms.hospital.service;

import com.hms.hospital.dto.request.PrescriptionCreateRequest;
import com.hms.hospital.entity.Prescription;

import java.util.List;

public interface PrescriptionService {
    Prescription create(PrescriptionCreateRequest request);
    List<Prescription> findByPatient(Long patient);
    List<Prescription> findAll();
}
