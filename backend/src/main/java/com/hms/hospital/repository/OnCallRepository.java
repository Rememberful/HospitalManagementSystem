package com.hms.hospital.repository;

import com.hms.hospital.entity.OnCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnCallRepository extends JpaRepository<OnCall, String> {
    List<OnCall> findByNurse(Integer nurse);
}
