package com.hms.hospital.controller;

import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    private final Physician physician = new Physician(1, "John Dorian", "Staff Internist", 111111111L);

    @Test
    void getReturnsTheRecord() throws Exception {
        when(adminService.get("physicians", 1L)).thenReturn(physician);

        mockMvc.perform(get("/api/admin/physicians/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("John Dorian"));
    }

    @Test
    void getUnknownRecordReturns404() throws Exception {
        when(adminService.get("physicians", 999L)).thenThrow(new ResourceNotFoundException("physicians"));

        mockMvc.perform(get("/api/admin/physicians/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUnknownResourceReturns400() throws Exception {
        when(adminService.get("billing", 1L)).thenThrow(new BadRequestException("Unknown or non-editable resource: billing"));

        mockMvc.perform(get("/api/admin/billing/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putMergesAndReturnsTheUpdatedRecord() throws Exception {
        Physician updated = new Physician(1, "Perry Cox", "Staff Internist", 111111111L);
        when(adminService.update(eq("physicians"), eq(1L), anyMap())).thenReturn(updated);

        mockMvc.perform(put("/api/admin/physicians/1")
                        .contentType("application/json")
                        .content("{\"Name\":\"Perry Cox\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("Perry Cox"));
    }

    @Test
    void putOnMissingRecordReturns404() throws Exception {
        when(adminService.update(eq("physicians"), eq(999L), anyMap())).thenThrow(new ResourceNotFoundException("physicians"));

        mockMvc.perform(put("/api/admin/physicians/999")
                        .contentType("application/json")
                        .content("{\"Name\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsConfirmationMessage() throws Exception {
        mockMvc.perform(delete("/api/admin/physicians/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successfully"));

        verify(adminService).delete("physicians", 1L);
    }

    @Test
    void deleteOnMissingRecordReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("physicians")).when(adminService).delete("physicians", 999L);

        mockMvc.perform(delete("/api/admin/physicians/999"))
                .andExpect(status().isNotFound());
    }
}
