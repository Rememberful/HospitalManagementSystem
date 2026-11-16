package com.hms.hospital.service;

import com.hms.hospital.dto.request.AppointmentCreateRequest;
import com.hms.hospital.dto.request.AppointmentUpdateRequest;
import com.hms.hospital.entity.Appointment;

import java.util.List;

public interface AppointmentService {
    Appointment book(AppointmentCreateRequest request);
    List<Appointment> findAll();
    Appointment getById(Long appointmentId);
    Appointment update(Long appointmentId, AppointmentUpdateRequest request);
}
