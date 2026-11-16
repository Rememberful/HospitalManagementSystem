package com.hms.hospital.repository;

import com.hms.hospital.entity.AffiliatedWith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AffiliatedWithRepository extends JpaRepository<AffiliatedWith, String> {
    List<AffiliatedWith> findByDepartment(Integer department);
    List<AffiliatedWith> findByPhysician(Integer physician);
    Optional<AffiliatedWith> findByPhysicianAndPrimaryAffiliation(Integer physician, Integer primaryAffiliation);
}
