package com.hms.hospital.repository;

import com.hms.hospital.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, String> {
    List<Billing> findByPatient(Long patient);
}
