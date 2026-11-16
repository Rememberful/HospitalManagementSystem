package com.hms.hospital.service;

import com.hms.hospital.dto.request.OnCallCreateRequest;
import com.hms.hospital.entity.OnCall;

import java.util.List;

public interface OnCallService {
    OnCall create(OnCallCreateRequest request);
    List<OnCall> findAll();
    void delete(String id);
}
