package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.NurseCreateRequest;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.service.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NurseServiceImpl implements NurseService {

    private final NurseRepository nurseRepository;

    @Override
    public Nurse create(NurseCreateRequest request) {
        if (nurseRepository.existsById(request.employeeID())) {
            throw new BadRequestException("Nurse with this EmployeeID already exists");
        }
        Nurse nurse = new Nurse(request.employeeID(), request.name(), request.position(),
                request.registered(), request.ssn());
        return nurseRepository.save(nurse);
    }

    @Override
    public List<Nurse> findAll() {
        return nurseRepository.findAll();
    }

    @Override
    public Nurse getByEmployeeId(Integer employeeId) {
        return nurseRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nurse"));
    }

    @Override
    public String getPosition(Integer employeeId) {
        return getByEmployeeId(employeeId).getPosition();
    }

    @Override
    public boolean isRegistered(Integer employeeId) {
        Integer registered = getByEmployeeId(employeeId).getRegistered();
        return registered != null && registered != 0;
    }

    @Override
    public Nurse updateRegistered(Integer employeeId, Integer registered) {
        Nurse nurse = getByEmployeeId(employeeId);
        nurse.setRegistered(registered);
        return nurseRepository.save(nurse);
    }

    @Override
    public Nurse updateSsn(Integer employeeId, Long ssn) {
        Nurse nurse = getByEmployeeId(employeeId);
        nurse.setSsn(ssn);
        return nurseRepository.save(nurse);
    }
}
