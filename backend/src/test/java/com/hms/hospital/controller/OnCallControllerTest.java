package com.hms.hospital.controller;

import com.hms.hospital.entity.OnCall;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.OnCallService;
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

@ControllerTest(OnCallController.class)
class OnCallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OnCallService onCallService;

    @Test
    void createReturns201() throws Exception {
        when(onCallService.create(any())).thenReturn(new OnCall("oc1", 101, 1, 1, "08:00", "20:00"));

        mockMvc.perform(post("/api/on_call")
                        .contentType("application/json")
                        .content("{\"nurse\":101,\"blockFloor\":1,\"blockCode\":1,\"onCallStart\":\"08:00\",\"onCallEnd\":\"20:00\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createRejectsUnknownNurseWith400() throws Exception {
        when(onCallService.create(any())).thenThrow(new BadRequestException("nurse must reference an existing Nurse"));

        mockMvc.perform(post("/api/on_call")
                        .contentType("application/json")
                        .content("{\"nurse\":999,\"blockFloor\":1,\"blockCode\":1,\"onCallStart\":\"08:00\",\"onCallEnd\":\"20:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllOnCallRecords() throws Exception {
        when(onCallService.findAll()).thenReturn(List.of(new OnCall("oc1", 101, 1, 1, "08:00", "20:00")));

        mockMvc.perform(get("/api/on_call"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("oc1"));
    }
}
