package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.BillingCreateRequest;
import com.hms.hospital.entity.Billing;
import com.hms.hospital.entity.BillingItem;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.BillingRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.StayRepository;
import com.hms.hospital.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final PatientRepository patientRepository;
    private final StayRepository stayRepository;

    @Override
    public Billing create(BillingCreateRequest request) {
        if (!patientRepository.existsById(request.patient())) {
            throw new BadRequestException("patient must reference an existing Patient");
        }
        if (request.stayId() != null && !stayRepository.existsById(request.stayId())) {
            throw new BadRequestException("stayId must reference an existing Stay");
        }
        double total = request.items().stream()
                .mapToDouble(BillingItem::getAmount)
                .sum();
        Billing billing = new Billing(UUID.randomUUID().toString(), request.patient(), request.stayId(),
                request.items(), Math.round(total * 100.0) / 100.0, false);
        return billingRepository.save(billing);
    }

    @Override
    public Billing markPaid(String id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing"));
        billing.setPaid(true);
        return billingRepository.save(billing);
    }

    @Override
    public List<Billing> findByPatient(Long patient) {
        return billingRepository.findByPatient(patient);
    }

    @Override
    public List<Billing> findAll() {
        return billingRepository.findAll();
    }
}
