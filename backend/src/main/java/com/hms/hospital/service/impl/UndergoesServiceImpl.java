package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.UndergoesCreateRequest;
import com.hms.hospital.entity.Undergoes;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.MedicalProcedureRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.StayRepository;
import com.hms.hospital.repository.UndergoesRepository;
import com.hms.hospital.service.UndergoesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UndergoesServiceImpl implements UndergoesService {

    private final UndergoesRepository undergoesRepository;
    private final PatientRepository patientRepository;
    private final MedicalProcedureRepository procedureRepository;
    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;
    private final StayRepository stayRepository;

    @Override
    public Undergoes record(UndergoesCreateRequest request) {
        if (!patientRepository.existsById(request.patient())) {
            throw new BadRequestException("patient must reference an existing Patient");
        }
        if (!procedureRepository.existsById(request.procedures())) {
            throw new BadRequestException("procedures must reference an existing Procedure");
        }
        if (!physicianRepository.existsById(request.physician())) {
            throw new BadRequestException("physician must reference an existing Physician");
        }
        if (request.stay() != null && !stayRepository.existsById(request.stay())) {
            throw new BadRequestException("stay must reference an existing Stay");
        }
        if (request.assistingNurse() != null && !nurseRepository.existsById(request.assistingNurse())) {
            throw new BadRequestException("assistingNurse must reference an existing Nurse");
        }
        Undergoes undergoes = new Undergoes(UUID.randomUUID().toString(), request.patient(), request.procedures(),
                request.stay(), request.dateUndergoes(), request.physician(), request.assistingNurse());
        return undergoesRepository.save(undergoes);
    }

    @Override
    public List<Undergoes> findAll() {
        return undergoesRepository.findAll();
    }
}
