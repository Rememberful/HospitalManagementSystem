package com.hms.hospital.repository;

import com.hms.hospital.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Integer> {
    Optional<Salary> findByPhysician(Integer physician);
}
