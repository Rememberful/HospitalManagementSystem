package com.hms.hospital.repository;

import com.hms.hospital.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(Long patientSsn);
    List<Appointment> findByPhysician(Integer physicianEmployeeId);
    List<Appointment> findByPrepNurse(Integer nurseEmployeeId);
    Optional<Appointment> findByPhysicianAndPatient(Integer physician, Long patient);
}
