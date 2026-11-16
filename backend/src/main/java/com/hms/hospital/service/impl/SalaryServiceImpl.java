package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.SalaryUpsertRequest;
import com.hms.hospital.entity.Salary;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.SalaryRepository;
import com.hms.hospital.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SalaryRepository salaryRepository;
    private final PhysicianRepository physicianRepository;

    @Override
    public Salary upsert(SalaryUpsertRequest request) {
        if (!physicianRepository.existsById(request.physician())) {
            throw new BadRequestException("physician must reference an existing Physician");
        }
        Salary salary = new Salary(request.physician(), request.baseSalary(), request.commissionRate());
        return salaryRepository.save(salary);
    }
}
