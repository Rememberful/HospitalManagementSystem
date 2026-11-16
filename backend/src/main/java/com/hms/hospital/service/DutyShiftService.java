package com.hms.hospital.service;

import com.hms.hospital.dto.request.DutyShiftCreateRequest;
import com.hms.hospital.entity.DutyShift;

import java.util.List;

public interface DutyShiftService {
    DutyShift create(DutyShiftCreateRequest request);
    List<DutyShift> findAll();
    void delete(String id);
}
