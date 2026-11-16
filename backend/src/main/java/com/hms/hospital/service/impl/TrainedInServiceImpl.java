package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.TrainedInWithProcedureView;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.TrainedIn;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.MedicalProcedureRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.TrainedInRepository;
import com.hms.hospital.service.TrainedInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainedInServiceImpl implements TrainedInService {

    private final TrainedInRepository trainedInRepository;
    private final PhysicianRepository physicianRepository;
    private final MedicalProcedureRepository procedureRepository;

    @Override
    public TrainedIn create(Integer physician, Integer treatment, String certificationDate, String certificationExpires) {
        if (physician == null || treatment == null || certificationDate == null || certificationExpires == null) {
            throw new BadRequestException("Physician, Treatment, CertificationDate and CertificationExpires are required");
        }
        if (!physicianRepository.existsById(physician)) {
            throw new BadRequestException("Physician must reference an existing Physician");
        }
        if (!procedureRepository.existsById(treatment)) {
            throw new BadRequestException("Treatment must reference an existing Procedure");
        }
        String id = physician + "-" + treatment;
        if (trainedInRepository.existsById(id)) {
            throw new BadRequestException("This physician is already certified for this treatment");
        }
        TrainedIn trainedIn = new TrainedIn(id, physician, treatment, certificationDate, certificationExpires);
        return trainedInRepository.save(trainedIn);
    }

    @Override
    public List<TrainedIn> findAll() {
        return trainedInRepository.findAll();
    }

    @Override
    public List<TrainedInWithProcedureView> treatmentsForPhysician(Integer physicianId) {
        return trainedInRepository.findByPhysician(physicianId).stream()
                .map(t -> TrainedInWithProcedureView.from(t, procedureRepository.findById(t.getTreatment()).orElse(null)))
                .toList();
    }

    @Override
    public List<Physician> physiciansForProcedure(Integer procedureId) {
        Set<Integer> ids = trainedInRepository.findByTreatment(procedureId).stream()
                .map(TrainedIn::getPhysician)
                .collect(Collectors.toSet());
        return physicianRepository.findAllById(ids);
    }
}
