package com.hms.hospital.repository;

import com.hms.hospital.entity.Physician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhysicianRepository extends JpaRepository<Physician, Integer> {
    Optional<Physician> findByNameIgnoreCase(String name);
    List<Physician> findByPositionIgnoreCase(String position);
}
