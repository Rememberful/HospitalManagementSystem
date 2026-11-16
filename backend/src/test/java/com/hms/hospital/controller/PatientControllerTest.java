package com.hms.hospital.controller;

import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.AppointmentService;
import com.hms.hospital.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;
    @MockBean
    private AppointmentService appointmentService;

    private final Patient patient = new Patient(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);

    @Test
    void postCreatesPatientAndReturns201() throws Exception {
        when(patientService.create(any())).thenReturn(patient);

        mockMvc.perform(post("/api/patient")
                        .contentType("application/json")
                        .content("{\"SSN\":100000001,\"Name\":\"John Smith\",\"Address\":\"42 Foobar Lane\",\"Phone\":\"555-0256\",\"InsuranceID\":68476213,\"PCP\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void postWithDuplicateSsnReturns400() throws Exception {
        when(patientService.create(any())).thenThrow(new BadRequestException("Patient with this SSN already exists"));

        mockMvc.perform(post("/api/patient")
                        .contentType("application/json")
                        .content("{\"SSN\":100000001,\"Name\":\"John Smith\",\"Address\":\"42 Foobar Lane\",\"Phone\":\"555-0256\",\"InsuranceID\":68476213,\"PCP\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllPatients() throws Exception {
        when(patientService.findAll()).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patient/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].SSN").value(100000001L));
    }

    @Test
    void getByPhysicianReturnsList() throws Exception {
        when(patientService.getByPhysician(1)).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patient/physician/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Name").value("John Smith"));
    }

    @Test
    void getByPhysicianAndPatientReturns200() throws Exception {
        when(patientService.getByPhysicianAndSsn(1, 100000001L)).thenReturn(patient);

        mockMvc.perform(get("/api/patient/physician/1/100000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SSN").value(100000001L));
    }

    @Test
    void getByPhysicianAndPatientMissingLinkReturns404() throws Exception {
        when(patientService.getByPhysicianAndSsn(1, 999L)).thenThrow(new ResourceNotFoundException("Patient"));

        mockMvc.perform(get("/api/patient/physician/1/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInsuranceReturnsWrappedValue() throws Exception {
        when(patientService.getInsuranceId(100000001L)).thenReturn(68476213L);

        mockMvc.perform(get("/api/patient/insurance/100000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.InsuranceID").value(68476213L));
    }

    @Test
    void updateAddressReturns200() throws Exception {
        Patient updated = new Patient(100000001L, "John Smith", "1 New Street", "555-0256", 68476213L, 1);
        when(patientService.updateAddress(eq(100000001L), eq("1 New Street"))).thenReturn(updated);

        mockMvc.perform(put("/api/patient/address/100000001")
                        .contentType("application/json")
                        .content("{\"Address\":\"1 New Street\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Address").value("1 New Street"));
    }

    @Test
    void updateAddressBlankReturns400() throws Exception {
        mockMvc.perform(put("/api/patient/address/100000001")
                        .contentType("application/json")
                        .content("{\"Address\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePhoneReturns200() throws Exception {
        Patient updated = new Patient(100000001L, "John Smith", "42 Foobar Lane", "555-9999", 68476213L, 1);
        when(patientService.updatePhone(eq(100000001L), eq("555-9999"))).thenReturn(updated);

        mockMvc.perform(put("/api/patient/phone/100000001")
                        .contentType("application/json")
                        .content("{\"Phone\":\"555-9999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Phone").value("555-9999"));
    }

    @Test
    void editOwnAppointmentReturns200() throws Exception {
        Appointment updated = new Appointment(1L, 100000001L, 101, 2, "s", "e", "B", "OPD");
        when(appointmentService.update(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/patient/appointment/1")
                        .contentType("application/json")
                        .content("{\"Physician\":2,\"ExaminationRoom\":\"B\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Physician").value(2));
    }

    @Test
    void editOwnAppointmentMissingReturns404() throws Exception {
        when(appointmentService.update(eq(404L), any())).thenThrow(new ResourceNotFoundException("Appointment"));

        mockMvc.perform(put("/api/patient/appointment/404")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
