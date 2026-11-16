package com.hms.hospital.service;

import com.hms.hospital.dto.request.ProcedureCreateRequest;
import com.hms.hospital.entity.MedicalProcedure;

import java.util.List;

public interface ProcedureService {
    MedicalProcedure create(ProcedureCreateRequest request);
    List<MedicalProcedure> findAll();
    Double getCost(Integer code);
    MedicalProcedure updateCost(Integer code, Double cost);
    MedicalProcedure updateName(Integer code, String name);
    List<MedicalProcedure> searchByName(String name);
}
