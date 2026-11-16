package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.DutyShiftCreateRequest;
import com.hms.hospital.entity.DutyShift;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.DutyShiftRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.service.DutyShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DutyShiftServiceImpl implements DutyShiftService {

    private final DutyShiftRepository dutyShiftRepository;
    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;

    @Override
    public DutyShift create(DutyShiftCreateRequest request) {
        String personType = request.personType().toLowerCase();
        if (!personType.equals("physician") && !personType.equals("nurse")) {
            throw new BadRequestException("personType must be 'physician' or 'nurse'");
        }
        boolean exists = personType.equals("physician")
                ? physicianRepository.existsById(request.personId())
                : nurseRepository.existsById(request.personId());
        if (!exists) {
            throw new BadRequestException("personId must reference an existing " + personType);
        }
        DutyShift shift = new DutyShift(UUID.randomUUID().toString(), personType, request.personId(),
                request.date(), request.joining(), request.leaving(),
                request.shiftType() == null ? "Regular" : request.shiftType());
        return dutyShiftRepository.save(shift);
    }

    @Override
    public List<DutyShift> findAll() {
        return dutyShiftRepository.findAll();
    }

    @Override
    public void delete(String id) {
        if (!dutyShiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("DutyShift");
        }
        dutyShiftRepository.deleteById(id);
    }
}
