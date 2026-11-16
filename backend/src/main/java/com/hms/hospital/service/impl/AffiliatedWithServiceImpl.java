package com.hms.hospital.service.impl;

import com.hms.hospital.entity.AffiliatedWith;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.AffiliatedWithRepository;
import com.hms.hospital.repository.DepartmentRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.service.AffiliatedWithService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffiliatedWithServiceImpl implements AffiliatedWithService {

    private final AffiliatedWithRepository affiliatedWithRepository;
    private final PhysicianRepository physicianRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public AffiliatedWith create(Integer physician, Integer department, Boolean primaryAffiliation) {
        if (physician == null || department == null) {
            throw new BadRequestException("Physician and Department are required");
        }
        if (!physicianRepository.existsById(physician)) {
            throw new BadRequestException("Physician must reference an existing Physician");
        }
        if (!departmentRepository.existsById(department)) {
            throw new BadRequestException("Department must reference an existing Department");
        }
        String id = physician + "-" + department;
        if (affiliatedWithRepository.existsById(id)) {
            throw new BadRequestException("This physician is already affiliated with this department");
        }
        AffiliatedWith link = new AffiliatedWith(id, physician, department,
                Boolean.TRUE.equals(primaryAffiliation) ? 1 : 0);
        return affiliatedWithRepository.save(link);
    }

    @Override
    public List<Physician> getPhysiciansForDepartment(Integer departmentId) {
        Set<Integer> ids = affiliatedWithRepository.findByDepartment(departmentId).stream()
                .map(AffiliatedWith::getPhysician)
                .collect(Collectors.toSet());
        return physicianRepository.findAllById(ids);
    }

    @Override
    public List<Department> getDepartmentsForPhysician(Integer physicianId) {
        Set<Integer> ids = affiliatedWithRepository.findByPhysician(physicianId).stream()
                .map(AffiliatedWith::getDepartment)
                .collect(Collectors.toSet());
        return departmentRepository.findAllById(ids);
    }

    @Override
    public Department getPrimaryAffiliation(Integer physicianId) {
        AffiliatedWith primary = affiliatedWithRepository.findByPhysicianAndPrimaryAffiliation(physicianId, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Primary affiliation"));
        return departmentRepository.findById(primary.getDepartment())
                .orElseThrow(() -> new ResourceNotFoundException("Department"));
    }
}
