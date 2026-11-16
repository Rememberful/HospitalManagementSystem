package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.DepartmentCreateRequest;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.DepartmentRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final PhysicianRepository physicianRepository;

    @Override
    public Department create(DepartmentCreateRequest request) {
        if (!physicianRepository.existsById(request.head())) {
            throw new BadRequestException("Head must reference an existing Physician");
        }
        if (departmentRepository.existsById(request.departmentID())) {
            throw new BadRequestException("Department with this DepartmentID already exists");
        }
        Department department = new Department(request.departmentID(), request.name(), request.head());
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Department getById(Integer departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department"));
    }

    @Override
    public Physician getHead(Integer departmentId) {
        Department department = getById(departmentId);
        return physicianRepository.findById(department.getHead())
                .orElseThrow(() -> new ResourceNotFoundException("Physician"));
    }

    @Override
    public boolean isPhysicianAHead(Integer physicianId) {
        return departmentRepository.findByHead(physicianId).isPresent();
    }

    @Override
    public Department updateHead(Integer departmentId, Integer newHeadEmployeeId) {
        if (!physicianRepository.existsById(newHeadEmployeeId)) {
            throw new BadRequestException("Head must reference an existing Physician");
        }
        Department department = getById(departmentId);
        department.setHead(newHeadEmployeeId);
        return departmentRepository.save(department);
    }
}
