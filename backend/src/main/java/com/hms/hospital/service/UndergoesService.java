package com.hms.hospital.service;

import com.hms.hospital.dto.request.UndergoesCreateRequest;
import com.hms.hospital.entity.Undergoes;

import java.util.List;

public interface UndergoesService {
    Undergoes record(UndergoesCreateRequest request);
    List<Undergoes> findAll();
}
