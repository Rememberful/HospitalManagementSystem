package com.hms.hospital.service;

import com.hms.hospital.dto.request.MedicationCreateRequest;
import com.hms.hospital.entity.Medication;

import java.util.List;

public interface MedicationService {
    Medication create(MedicationCreateRequest request);
    List<Medication> findAll();
}
