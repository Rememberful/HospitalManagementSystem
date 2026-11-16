package com.hms.hospital.service;

import com.hms.hospital.entity.AffiliatedWith;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;

import java.util.List;

public interface AffiliatedWithService {
    AffiliatedWith create(Integer physician, Integer department, Boolean primaryAffiliation);
    List<Physician> getPhysiciansForDepartment(Integer departmentId);
    List<Department> getDepartmentsForPhysician(Integer physicianId);
    Department getPrimaryAffiliation(Integer physicianId);
}
