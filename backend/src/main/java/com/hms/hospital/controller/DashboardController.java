package com.hms.hospital.controller;

import com.hms.hospital.dto.response.DoctorDashboardResponse;
import com.hms.hospital.dto.response.NurseDashboardResponse;
import com.hms.hospital.dto.response.PatientDashboardResponse;
import com.hms.hospital.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/patient/{ssn}/dashboard")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(#ssn)")
    public PatientDashboardResponse patientDashboard(@PathVariable Long ssn) {
        return dashboardService.patientDashboard(ssn);
    }

    @GetMapping("/doctor/{empid}/dashboard")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfDoctor(#empid)")
    public DoctorDashboardResponse doctorDashboard(@PathVariable Integer empid) {
        return dashboardService.doctorDashboard(empid);
    }

    @GetMapping("/nurse/{empid}/dashboard")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfNurse(#empid)")
    public NurseDashboardResponse nurseDashboard(@PathVariable Integer empid) {
        return dashboardService.nurseDashboard(empid);
    }
}
