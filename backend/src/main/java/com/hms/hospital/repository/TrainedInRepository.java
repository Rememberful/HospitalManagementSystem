package com.hms.hospital.repository;

import com.hms.hospital.entity.TrainedIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainedInRepository extends JpaRepository<TrainedIn, String> {
    List<TrainedIn> findByPhysician(Integer physician);
    List<TrainedIn> findByTreatment(Integer treatment);
}
