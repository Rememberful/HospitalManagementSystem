package com.hms.hospital.controller;

import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.PhysicianService;
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

@ControllerTest(PhysicianController.class)
class PhysicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhysicianService physicianService;

    private final Physician physician = new Physician(1, "John Dorian", "Staff Internist", 111111111L);

    @Test
    void postCreatesPhysicianAndReturns201() throws Exception {
        when(physicianService.create(any())).thenReturn(physician);

        mockMvc.perform(post("/api/physician/post")
                        .contentType("application/json")
                        .content("{\"EmployeeID\":1,\"Name\":\"John Dorian\",\"Position\":\"Staff Internist\",\"SSN\":111111111}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record Created Successfully"));
    }

    @Test
    void postWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/physician/post")
                        .contentType("application/json")
                        .content("{\"Name\":\"John Dorian\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWithDuplicateEmployeeIdReturns400() throws Exception {
        when(physicianService.create(any())).thenThrow(new BadRequestException("Physician with this EmployeeID already exists"));

        mockMvc.perform(post("/api/physician/post")
                        .contentType("application/json")
                        .content("{\"EmployeeID\":1,\"Name\":\"John Dorian\",\"Position\":\"Staff Internist\",\"SSN\":111111111}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Physician with this EmployeeID already exists"));
    }

    @Test
    void getByEmployeeIdReturns200() throws Exception {
        when(physicianService.getByEmployeeId(1)).thenReturn(physician);

        mockMvc.perform(get("/api/physician/empid/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("John Dorian"));
    }

    @Test
    void getByEmployeeIdMissingReturns404() throws Exception {
        when(physicianService.getByEmployeeId(999)).thenThrow(new ResourceNotFoundException("Physician"));

        mockMvc.perform(get("/api/physician/empid/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Physician not found"));
    }

    @Test
    void getByNameReturns200() throws Exception {
        when(physicianService.getByName("John Dorian")).thenReturn(physician);

        mockMvc.perform(get("/api/physician/name/John Dorian"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EmployeeID").value(1));
    }

    @Test
    void getByPositionReturnsList() throws Exception {
        when(physicianService.getByPosition("Staff Internist")).thenReturn(List.of(physician));

        mockMvc.perform(get("/api/physician/position/Staff Internist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].EmployeeID").value(1));
    }

    @Test
    void updateNameReturns200() throws Exception {
        Physician updated = new Physician(1, "Perry Cox", "Staff Internist", 111111111L);
        when(physicianService.updateName(eq(1), eq("Perry Cox"))).thenReturn(updated);

        mockMvc.perform(put("/api/physician/update/name/1")
                        .contentType("application/json")
                        .content("{\"Name\":\"Perry Cox\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("Perry Cox"));
    }

    @Test
    void updateNameWithBlankNameReturns400() throws Exception {
        mockMvc.perform(put("/api/physician/update/name/1")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSsnReturns200() throws Exception {
        Physician updated = new Physician(1, "John Dorian", "Staff Internist", 999999999L);
        when(physicianService.updateSsn(eq(1), eq(999999999L))).thenReturn(updated);

        mockMvc.perform(put("/api/physician/update/ssn/1")
                        .contentType("application/json")
                        .content("{\"SSN\":999999999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SSN").value(999999999L));
    }
}
