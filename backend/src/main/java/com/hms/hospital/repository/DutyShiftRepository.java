package com.hms.hospital.repository;

import com.hms.hospital.entity.DutyShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DutyShiftRepository extends JpaRepository<DutyShift, String> {
    List<DutyShift> findByPersonTypeAndPersonId(String personType, Integer personId);
}
