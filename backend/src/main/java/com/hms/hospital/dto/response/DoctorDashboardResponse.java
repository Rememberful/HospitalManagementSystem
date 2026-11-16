package com.hms.hospital.dto.response;

import com.hms.hospital.entity.DutyShift;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.Appointment;

import java.util.List;

public record DoctorDashboardResponse(
        Physician profile,
        List<Patient> patients,
        List<Appointment> appointments,
        List<OperationView> operations,
        long generalCheckups,
        List<DutyShift> dutyShifts,
        SalaryView salary) {
}
