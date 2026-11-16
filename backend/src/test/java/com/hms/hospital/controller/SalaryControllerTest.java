package com.hms.hospital.controller;

import com.hms.hospital.entity.Salary;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.SalaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(SalaryController.class)
class SalaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalaryService salaryService;

    @Test
    void upsertReturns200() throws Exception {
        when(salaryService.upsert(any())).thenReturn(new Salary(3, 9500.0, 0.04));

        mockMvc.perform(post("/api/salary")
                        .contentType("application/json")
                        .content("{\"physician\":3,\"baseSalary\":9500.0,\"commissionRate\":0.04}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseSalary").value(9500.0));
    }

    @Test
    void upsertRejectsUnknownPhysicianWith400() throws Exception {
        when(salaryService.upsert(any())).thenThrow(new BadRequestException("physician must reference an existing Physician"));

        mockMvc.perform(post("/api/salary")
                        .contentType("application/json")
                        .content("{\"physician\":999,\"baseSalary\":9500.0,\"commissionRate\":0.04}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsertMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/salary")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
