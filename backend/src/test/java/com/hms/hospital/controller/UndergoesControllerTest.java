package com.hms.hospital.controller;

import com.hms.hospital.entity.Undergoes;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.UndergoesService;
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

@ControllerTest(UndergoesController.class)
class UndergoesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UndergoesService undergoesService;

    @Test
    void recordReturns201() throws Exception {
        when(undergoesService.record(any()))
                .thenReturn(new Undergoes("u1", 100000001L, 6, 3215, "2030-01-01", 3, 101));

        mockMvc.perform(post("/api/undergoes")
                        .contentType("application/json")
                        .content("{\"patient\":100000001,\"procedures\":6,\"stay\":3215,\"dateUndergoes\":\"2030-01-01\",\"physician\":3,\"assistingNurse\":101}"))
                .andExpect(status().isCreated());
    }

    @Test
    void recordRejectsUnknownProcedureWith400() throws Exception {
        when(undergoesService.record(any())).thenThrow(new BadRequestException("procedures must reference an existing Procedure"));

        mockMvc.perform(post("/api/undergoes")
                        .contentType("application/json")
                        .content("{\"patient\":100000001,\"procedures\":999,\"dateUndergoes\":\"2030-01-01\",\"physician\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllRecords() throws Exception {
        when(undergoesService.findAll())
                .thenReturn(List.of(new Undergoes("u1", 100000001L, 6, 3215, "2030-01-01", 3, 101)));

        mockMvc.perform(get("/api/undergoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("u1"));
    }
}
