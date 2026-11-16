package com.hms.hospital.controller;

import com.hms.hospital.entity.Nurse;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.NurseService;
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

@ControllerTest(NurseController.class)
class NurseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NurseService nurseService;

    private final Nurse nurse = new Nurse(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);

    @Test
    void postCreatesNurseAndReturns201() throws Exception {
        when(nurseService.create(any())).thenReturn(nurse);

        mockMvc.perform(post("/api/nurse")
                        .contentType("application/json")
                        .content("{\"EmployeeID\":101,\"Name\":\"Carla Espinosa\",\"Position\":\"Head Nurse\",\"Registered\":1,\"SSN\":111111110}"))
                .andExpect(status().isCreated());
    }

    @Test
    void postWithDuplicateEmployeeIdReturns400() throws Exception {
        when(nurseService.create(any())).thenThrow(new BadRequestException("Nurse with this EmployeeID already exists"));

        mockMvc.perform(post("/api/nurse")
                        .contentType("application/json")
                        .content("{\"EmployeeID\":101,\"Name\":\"Carla Espinosa\",\"Position\":\"Head Nurse\",\"Registered\":1,\"SSN\":111111110}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllNurses() throws Exception {
        when(nurseService.findAll()).thenReturn(List.of(nurse));

        mockMvc.perform(get("/api/nurse/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].EmployeeID").value(101));
    }

    @Test
    void getByEmployeeIdReturns200() throws Exception {
        when(nurseService.getByEmployeeId(101)).thenReturn(nurse);

        mockMvc.perform(get("/api/nurse/empid/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("Carla Espinosa"));
    }

    @Test
    void getByEmployeeIdMissingReturns404() throws Exception {
        when(nurseService.getByEmployeeId(999)).thenThrow(new ResourceNotFoundException("Nurse"));

        mockMvc.perform(get("/api/nurse/empid/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPositionReturnsWrappedValue() throws Exception {
        when(nurseService.getPosition(101)).thenReturn("Head Nurse");

        mockMvc.perform(get("/api/nurse/position/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Position").value("Head Nurse"));
    }

    @Test
    void isRegisteredReturnsWrappedBoolean() throws Exception {
        when(nurseService.isRegistered(101)).thenReturn(true);

        mockMvc.perform(get("/api/nurse/registered/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Registered").value(true));
    }

    @Test
    void updateRegisteredReturns200() throws Exception {
        Nurse updated = new Nurse(101, "Carla Espinosa", "Head Nurse", 0, 111111110L);
        when(nurseService.updateRegistered(eq(101), eq(0))).thenReturn(updated);

        mockMvc.perform(put("/api/nurse/registered/101")
                        .contentType("application/json")
                        .content("{\"Registered\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Registered").value(0));
    }

    @Test
    void updateRegisteredMissingFieldReturns400() throws Exception {
        mockMvc.perform(put("/api/nurse/registered/101")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSsnReturns200() throws Exception {
        Nurse updated = new Nurse(101, "Carla Espinosa", "Head Nurse", 1, 999999999L);
        when(nurseService.updateSsn(eq(101), eq(999999999L))).thenReturn(updated);

        mockMvc.perform(put("/api/nurse/ssn/101")
                        .contentType("application/json")
                        .content("{\"SSN\":999999999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SSN").value(999999999L));
    }
}
