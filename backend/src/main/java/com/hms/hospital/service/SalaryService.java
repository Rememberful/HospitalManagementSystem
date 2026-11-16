package com.hms.hospital.service;

import com.hms.hospital.dto.request.SalaryUpsertRequest;
import com.hms.hospital.entity.Salary;

public interface SalaryService {
    /** Creates the salary record if none exists for this physician yet, otherwise updates it. */
    Salary upsert(SalaryUpsertRequest request);
}
