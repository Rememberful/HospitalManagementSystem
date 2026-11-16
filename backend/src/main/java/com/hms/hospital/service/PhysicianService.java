package com.hms.hospital.service;

import com.hms.hospital.dto.request.PhysicianCreateRequest;
import com.hms.hospital.entity.Physician;

import java.util.List;

public interface PhysicianService {
    Physician create(PhysicianCreateRequest request);
    Physician getByEmployeeId(Integer employeeId);
    Physician getByName(String name);
    List<Physician> getByPosition(String position);
    Physician updateName(Integer employeeId, String name);
    Physician updateSsn(Integer employeeId, Long ssn);
    Physician updatePosition(Integer employeeId, String position);
    List<Physician> findAll();
}
