package com.hms.hospital.repository;

import com.hms.hospital.entity.Stay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StayRepository extends JpaRepository<Stay, Integer> {
    List<Stay> findByPatient(Long patient);
}
