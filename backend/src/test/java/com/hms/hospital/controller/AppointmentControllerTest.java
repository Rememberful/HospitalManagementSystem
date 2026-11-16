package com.hms.hospital.controller;

import com.hms.hospital.entity.Appointment;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    private final Appointment appointment = new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD");

    @Test
    void bookCreatesAppointmentAndReturns201() throws Exception {
        when(appointmentService.book(any())).thenReturn(appointment);

        mockMvc.perform(post("/api/appointment/")
                        .contentType("application/json")
                        .content("{\"AppointmentID\":1,\"Patient\":100000001,\"Physician\":1,\"Starto\":\"s\",\"Endo\":\"e\",\"ExaminationRoom\":\"A\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record Created Successfully"));
    }

    @Test
    void bookWithoutTrailingSlashAlsoWorks() throws Exception {
        when(appointmentService.book(any())).thenReturn(appointment);

        mockMvc.perform(post("/api/appointment")
                        .contentType("application/json")
                        .content("{\"AppointmentID\":1,\"Patient\":100000001,\"Physician\":1,\"Starto\":\"s\",\"Endo\":\"e\",\"ExaminationRoom\":\"A\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void bookMissingRequiredFieldReturns400() throws Exception {
        mockMvc.perform(post("/api/appointment/")
                        .contentType("application/json")
                        .content("{\"Patient\":100000001}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookRejectsUnknownPatientWith400() throws Exception {
        when(appointmentService.book(any())).thenThrow(new BadRequestException("Patient must reference an existing Patient SSN"));

        mockMvc.perform(post("/api/appointment/")
                        .contentType("application/json")
                        .content("{\"AppointmentID\":1,\"Patient\":999,\"Physician\":1,\"Starto\":\"s\",\"Endo\":\"e\",\"ExaminationRoom\":\"A\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Patient must reference an existing Patient SSN"));
    }

    @Test
    void listReturnsAllAppointmentsWithoutTrailingSlash() throws Exception {
        when(appointmentService.findAll()).thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/appointment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].AppointmentID").value(1));
    }

    @Test
    void listReturnsAllAppointmentsWithTrailingSlash() throws Exception {
        when(appointmentService.findAll()).thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/appointment/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].AppointmentID").value(1));
    }
}
