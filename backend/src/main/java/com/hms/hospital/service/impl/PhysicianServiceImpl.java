package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.PhysicianCreateRequest;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.service.PhysicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhysicianServiceImpl implements PhysicianService {

    private final PhysicianRepository physicianRepository;

    @Override
    public Physician create(PhysicianCreateRequest request) {
        if (physicianRepository.existsById(request.employeeID())) {
            throw new BadRequestException("Physician with this EmployeeID already exists");
        }
        Physician physician = new Physician(
                request.employeeID(), request.name(), request.position(), request.ssn());
        return physicianRepository.save(physician);
    }

    @Override
    public Physician getByEmployeeId(Integer employeeId) {
        return physicianRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Physician"));
    }

    @Override
    public Physician getByName(String name) {
        return physicianRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Physician"));
    }

    @Override
    public List<Physician> getByPosition(String position) {
        return physicianRepository.findByPositionIgnoreCase(position);
    }

    @Override
    public Physician updateName(Integer employeeId, String name) {
        Physician physician = getByEmployeeId(employeeId);
        physician.setName(name);
        return physicianRepository.save(physician);
    }

    @Override
    public Physician updateSsn(Integer employeeId, Long ssn) {
        Physician physician = getByEmployeeId(employeeId);
        physician.setSsn(ssn);
        return physicianRepository.save(physician);
    }

    @Override
    public Physician updatePosition(Integer employeeId, String position) {
        Physician physician = getByEmployeeId(employeeId);
        physician.setPosition(position);
        return physicianRepository.save(physician);
    }

    @Override
    public List<Physician> findAll() {
        return physicianRepository.findAll();
    }
}
