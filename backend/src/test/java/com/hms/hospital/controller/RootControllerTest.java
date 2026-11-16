package com.hms.hospital.controller;

import com.hms.hospital.entity.Physician;
import com.hms.hospital.service.PhysicianService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(RootController.class)
class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhysicianService physicianService;

    @Test
    void updatePositionReturns200() throws Exception {
        Physician updated = new Physician(1, "John Dorian", "Attending Physician", 111111111L);
        when(physicianService.updatePosition(eq(1), eq("Attending Physician"))).thenReturn(updated);

        mockMvc.perform(put("/update/position/Attending Physician/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Position").value("Attending Physician"));
    }
}
