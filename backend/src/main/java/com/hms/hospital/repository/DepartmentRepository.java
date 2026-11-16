package com.hms.hospital.repository;

import com.hms.hospital.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByHead(Integer head);
}
