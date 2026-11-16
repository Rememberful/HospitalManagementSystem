package com.hms.hospital.service;

import com.hms.hospital.dto.request.BillingCreateRequest;
import com.hms.hospital.entity.Billing;

import java.util.List;

public interface BillingService {
    Billing create(BillingCreateRequest request);
    Billing markPaid(String id);
    List<Billing> findByPatient(Long patient);
    List<Billing> findAll();
}
