package com.hms.hospital.controller;

import com.hms.hospital.entity.Stay;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.StayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(StayController.class)
class StayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StayService stayService;

    @Test
    void admitReturns201() throws Exception {
        when(stayService.admit(any())).thenReturn(new Stay(3215, 100000001L, 101, "2030-01-01", null));

        mockMvc.perform(post("/api/stay/admit")
                        .contentType("application/json")
                        .content("{\"stayId\":3215,\"patient\":100000001,\"room\":101,\"stayStart\":\"2030-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Patient admitted successfully"));
    }

    @Test
    void admitRejectsUnavailableRoomWith400() throws Exception {
        when(stayService.admit(any())).thenThrow(new BadRequestException("This room is currently unavailable"));

        mockMvc.perform(post("/api/stay/admit")
                        .contentType("application/json")
                        .content("{\"stayId\":3215,\"patient\":100000001,\"room\":101,\"stayStart\":\"2030-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This room is currently unavailable"));
    }

    @Test
    void dischargeReturns200() throws Exception {
        Stay discharged = new Stay(3215, 100000001L, 101, "2030-01-01", "2030-01-05");
        when(stayService.discharge(eq(3215), eq("2030-01-05"))).thenReturn(discharged);

        mockMvc.perform(put("/api/stay/discharge/3215")
                        .contentType("application/json")
                        .content("{\"StayEnd\":\"2030-01-05\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.StayEnd").value("2030-01-05"));
    }

    @Test
    void dischargeMissingStayReturns404() throws Exception {
        when(stayService.discharge(eq(404), any())).thenThrow(new ResourceNotFoundException("Stay"));

        mockMvc.perform(put("/api/stay/discharge/404")
                        .contentType("application/json")
                        .content("{\"StayEnd\":\"2030-01-05\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void dischargeWithoutStayEndReturns400() throws Exception {
        mockMvc.perform(put("/api/stay/discharge/3215")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
