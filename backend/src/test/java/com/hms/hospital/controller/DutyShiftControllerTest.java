package com.hms.hospital.controller;

import com.hms.hospital.entity.DutyShift;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.DutyShiftService;
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

@ControllerTest(DutyShiftController.class)
class DutyShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DutyShiftService dutyShiftService;

    @Test
    void createReturns201() throws Exception {
        when(dutyShiftService.create(any()))
                .thenReturn(new DutyShift("ds1", "nurse", 101, "2030-01-01", "08:00", "16:00", "Regular"));

        mockMvc.perform(post("/api/duty_shift")
                        .contentType("application/json")
                        .content("{\"personType\":\"nurse\",\"personId\":101,\"date\":\"2030-01-01\",\"joining\":\"08:00\",\"leaving\":\"16:00\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createRejectsInvalidPersonTypeWith400() throws Exception {
        when(dutyShiftService.create(any())).thenThrow(new BadRequestException("personType must be 'physician' or 'nurse'"));

        mockMvc.perform(post("/api/duty_shift")
                        .contentType("application/json")
                        .content("{\"personType\":\"janitor\",\"personId\":1,\"date\":\"2030-01-01\",\"joining\":\"08:00\",\"leaving\":\"16:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllShifts() throws Exception {
        when(dutyShiftService.findAll())
                .thenReturn(List.of(new DutyShift("ds1", "nurse", 101, "2030-01-01", "08:00", "16:00", "Regular")));

        mockMvc.perform(get("/api/duty_shift"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personType").value("nurse"));
    }
}
