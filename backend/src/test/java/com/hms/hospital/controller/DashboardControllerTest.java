package com.hms.hospital.controller;

import com.hms.hospital.dto.response.DoctorDashboardResponse;
import com.hms.hospital.dto.response.NurseDashboardResponse;
import com.hms.hospital.dto.response.PatientDashboardResponse;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void patientDashboardReturns200() throws Exception {
        Patient patient = new Patient(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);
        PatientDashboardResponse response = new PatientDashboardResponse(patient, null, List.of(), List.of(), List.of(), List.of());
        when(dashboardService.patientDashboard(100000001L)).thenReturn(response);

        mockMvc.perform(get("/api/patient/100000001/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.Name").value("John Smith"));
    }

    @Test
    void patientDashboardMissingReturns404() throws Exception {
        when(dashboardService.patientDashboard(999L)).thenThrow(new ResourceNotFoundException("Patient"));

        mockMvc.perform(get("/api/patient/999/dashboard"))
                .andExpect(status().isNotFound());
    }

    @Test
    void doctorDashboardReturns200() throws Exception {
        Physician physician = new Physician(3, "Christopher Turk", "Surgeon", 333333333L);
        DoctorDashboardResponse response = new DoctorDashboardResponse(physician, List.of(), List.of(), List.of(), 0L, List.of(), null);
        when(dashboardService.doctorDashboard(3)).thenReturn(response);

        mockMvc.perform(get("/api/doctor/3/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.EmployeeID").value(3));
    }

    @Test
    void doctorDashboardMissingReturns404() throws Exception {
        when(dashboardService.doctorDashboard(999)).thenThrow(new ResourceNotFoundException("Physician"));

        mockMvc.perform(get("/api/doctor/999/dashboard"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nurseDashboardReturns200() throws Exception {
        Nurse nurse = new Nurse(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);
        NurseDashboardResponse response = new NurseDashboardResponse(nurse, List.of(), List.of(), List.of());
        when(dashboardService.nurseDashboard(101)).thenReturn(response);

        mockMvc.perform(get("/api/nurse/101/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.EmployeeID").value(101));
    }

    @Test
    void nurseDashboardMissingReturns404() throws Exception {
        when(dashboardService.nurseDashboard(999)).thenThrow(new ResourceNotFoundException("Nurse"));

        mockMvc.perform(get("/api/nurse/999/dashboard"))
                .andExpect(status().isNotFound());
    }
}
