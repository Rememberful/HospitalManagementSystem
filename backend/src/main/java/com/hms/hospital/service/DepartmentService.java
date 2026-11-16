package com.hms.hospital.service;

import com.hms.hospital.dto.request.DepartmentCreateRequest;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;

import java.util.List;

public interface DepartmentService {
    Department create(DepartmentCreateRequest request);
    List<Department> findAll();
    Department getById(Integer departmentId);
    Physician getHead(Integer departmentId);
    boolean isPhysicianAHead(Integer physicianId);
    Department updateHead(Integer departmentId, Integer newHeadEmployeeId);
}
