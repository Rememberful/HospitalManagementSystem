package com.hms.hospital.controller;

import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.ProcedureService;
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

@ControllerTest(ProcedureController.class)
class ProcedureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcedureService procedureService;

    private final MedicalProcedure procedure = new MedicalProcedure(1, "Reverse Rhinopodoplasty", 1500.0);

    @Test
    void postCreatesProcedureAndReturns201() throws Exception {
        when(procedureService.create(any())).thenReturn(procedure);

        mockMvc.perform(post("/api/procedure")
                        .contentType("application/json")
                        .content("{\"Code\":1,\"Name\":\"Reverse Rhinopodoplasty\",\"Cost\":1500.0}"))
                .andExpect(status().isCreated());
    }

    @Test
    void postWithDuplicateCodeReturns400() throws Exception {
        when(procedureService.create(any())).thenThrow(new BadRequestException("Procedure with this Code already exists"));

        mockMvc.perform(post("/api/procedure")
                        .contentType("application/json")
                        .content("{\"Code\":1,\"Name\":\"Reverse Rhinopodoplasty\",\"Cost\":1500.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllProcedures() throws Exception {
        when(procedureService.findAll()).thenReturn(List.of(procedure));

        mockMvc.perform(get("/api/procedure/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Code").value(1));
    }

    @Test
    void getCostReturnsCodeAndCost() throws Exception {
        when(procedureService.getCost(1)).thenReturn(1500.0);

        mockMvc.perform(get("/api/procedure/cost/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Code").value(1))
                .andExpect(jsonPath("$.Cost").value(1500.0));
    }

    @Test
    void getCostMissingReturns404() throws Exception {
        when(procedureService.getCost(99)).thenThrow(new ResourceNotFoundException("Procedure"));

        mockMvc.perform(get("/api/procedure/cost/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCostReturns200() throws Exception {
        MedicalProcedure updated = new MedicalProcedure(1, "Reverse Rhinopodoplasty", 2000.0);
        when(procedureService.updateCost(eq(1), eq(2000.0))).thenReturn(updated);

        mockMvc.perform(put("/api/procedure/cost/1")
                        .contentType("application/json")
                        .content("{\"Cost\":2000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Cost").value(2000.0));
    }

    @Test
    void updateCostMissingFieldReturns400() throws Exception {
        mockMvc.perform(put("/api/procedure/cost/1")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNameReturns200() throws Exception {
        MedicalProcedure updated = new MedicalProcedure(1, "Renamed Procedure", 1500.0);
        when(procedureService.updateName(eq(1), eq("Renamed Procedure"))).thenReturn(updated);

        mockMvc.perform(put("/api/procedure/name/1")
                        .contentType("application/json")
                        .content("{\"Name\":\"Renamed Procedure\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("Renamed Procedure"));
    }

    @Test
    void searchReturnsMatchingProcedures() throws Exception {
        when(procedureService.searchByName("rhino")).thenReturn(List.of(procedure));

        mockMvc.perform(get("/api/procedure/search/rhino"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Name").value("Reverse Rhinopodoplasty"));
    }
}
