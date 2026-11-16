package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.MedicationCreateRequest;
import com.hms.hospital.entity.Medication;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.MedicationRepository;
import com.hms.hospital.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;

    @Override
    public Medication create(MedicationCreateRequest request) {
        if (medicationRepository.existsById(request.code())) {
            throw new BadRequestException("Medication with this Code already exists");
        }
        Medication medication = new Medication(request.code(), request.name(), request.brand(), request.description());
        return medicationRepository.save(medication);
    }

    @Override
    public List<Medication> findAll() {
        return medicationRepository.findAll();
    }
}
