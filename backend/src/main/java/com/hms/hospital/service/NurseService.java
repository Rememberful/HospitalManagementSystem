package com.hms.hospital.service;

import com.hms.hospital.dto.request.NurseCreateRequest;
import com.hms.hospital.entity.Nurse;

import java.util.List;

public interface NurseService {
    Nurse create(NurseCreateRequest request);
    List<Nurse> findAll();
    Nurse getByEmployeeId(Integer employeeId);
    String getPosition(Integer employeeId);
    boolean isRegistered(Integer employeeId);
    Nurse updateRegistered(Integer employeeId, Integer registered);
    Nurse updateSsn(Integer employeeId, Long ssn);
}
