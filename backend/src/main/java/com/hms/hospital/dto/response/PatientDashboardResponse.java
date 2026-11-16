package com.hms.hospital.dto.response;

import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Billing;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.Prescription;
import com.hms.hospital.entity.Stay;

import java.util.List;

public record PatientDashboardResponse(
        Patient profile,
        Physician primaryCarePhysician,
        List<Appointment> appointments,
        List<Stay> stays,
        List<Billing> billing,
        List<Prescription> prescriptions) {
}
