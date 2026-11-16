package com.hms.hospital.repository;

import com.hms.hospital.entity.Undergoes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UndergoesRepository extends JpaRepository<Undergoes, String> {
    List<Undergoes> findByPhysician(Integer physician);
    List<Undergoes> findByAssistingNurse(Integer assistingNurse);
}
