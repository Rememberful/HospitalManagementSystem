package com.hms.hospital.controller;

import com.hms.hospital.dto.response.HospitalStatsResponse;
import com.hms.hospital.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Test
    void statsReturns200WithAllFields() throws Exception {
        HospitalStatsResponse stats = new HospitalStatsResponse(5, 9, 3, 3, 2, 8, 1250.50, 1, 4, 12, 2);
        when(statsService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(5))
                .andExpect(jsonPath("$.totalPhysicians").value(9))
                .andExpect(jsonPath("$.totalRevenue").value(1250.50))
                .andExpect(jsonPath("$.occupiedRooms").value(4))
                .andExpect(jsonPath("$.totalRooms").value(12))
                .andExpect(jsonPath("$.activeAdmissions").value(2));
    }
}
