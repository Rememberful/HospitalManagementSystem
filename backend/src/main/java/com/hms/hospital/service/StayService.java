package com.hms.hospital.service;

import com.hms.hospital.dto.request.StayAdmitRequest;
import com.hms.hospital.entity.Stay;

import java.util.List;

public interface StayService {
    Stay admit(StayAdmitRequest request);
    Stay discharge(Integer stayId, String stayEnd);
    List<Stay> findAll();
}
