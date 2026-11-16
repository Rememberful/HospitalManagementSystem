package com.hms.hospital.controller;

import com.hms.hospital.dto.request.SalaryUpsertRequest;
import com.hms.hospital.entity.Salary;
import com.hms.hospital.service.SalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping
    public Salary upsert(@Valid @RequestBody SalaryUpsertRequest request) {
        return salaryService.upsert(request);
    }
}
