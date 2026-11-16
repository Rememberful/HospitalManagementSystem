package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.ProcedureCreateRequest;
import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.MedicalProcedureRepository;
import com.hms.hospital.service.ProcedureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcedureServiceImpl implements ProcedureService {

    private final MedicalProcedureRepository procedureRepository;

    @Override
    public MedicalProcedure create(ProcedureCreateRequest request) {
        if (procedureRepository.existsById(request.code())) {
            throw new BadRequestException("Procedure with this Code already exists");
        }
        MedicalProcedure procedure = new MedicalProcedure(request.code(), request.name(), request.cost());
        return procedureRepository.save(procedure);
    }

    @Override
    public List<MedicalProcedure> findAll() {
        return procedureRepository.findAll();
    }

    @Override
    public Double getCost(Integer code) {
        return procedureRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Procedure"))
                .getCost();
    }

    @Override
    public MedicalProcedure updateCost(Integer code, Double cost) {
        MedicalProcedure procedure = procedureRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Procedure"));
        procedure.setCost(cost);
        return procedureRepository.save(procedure);
    }

    @Override
    public MedicalProcedure updateName(Integer code, String name) {
        MedicalProcedure procedure = procedureRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Procedure"));
        procedure.setName(name);
        return procedureRepository.save(procedure);
    }

    @Override
    public List<MedicalProcedure> searchByName(String name) {
        return procedureRepository.findByNameContainingIgnoreCase(name);
    }
}
