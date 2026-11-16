package com.hms.hospital.repository;

import com.hms.hospital.entity.MedicalProcedure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalProcedureRepository extends JpaRepository<MedicalProcedure, Integer> {
    List<MedicalProcedure> findByNameContainingIgnoreCase(String name);
}
