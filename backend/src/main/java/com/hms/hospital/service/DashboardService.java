package com.hms.hospital.service;

import com.hms.hospital.dto.response.DoctorDashboardResponse;
import com.hms.hospital.dto.response.NurseDashboardResponse;
import com.hms.hospital.dto.response.PatientDashboardResponse;

public interface DashboardService {
    PatientDashboardResponse patientDashboard(Long ssn);
    DoctorDashboardResponse doctorDashboard(Integer employeeId);
    NurseDashboardResponse nurseDashboard(Integer employeeId);
}
