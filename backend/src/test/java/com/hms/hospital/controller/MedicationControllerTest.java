package com.hms.hospital.controller;

import com.hms.hospital.entity.Medication;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.MedicationService;
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

@ControllerTest(MedicationController.class)
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationService medicationService;

    private final Medication medication = new Medication(1, "Ibuprofen", "Advil", "NSAID");

    @Test
    void createReturns201() throws Exception {
        when(medicationService.create(any())).thenReturn(medication);

        mockMvc.perform(post("/api/medication")
                        .contentType("application/json")
                        .content("{\"Code\":1,\"Name\":\"Ibuprofen\",\"Brand\":\"Advil\",\"Description\":\"NSAID\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record Created Successfully"));
    }

    @Test
    void createWithDuplicateCodeReturns400() throws Exception {
        when(medicationService.create(any())).thenThrow(new BadRequestException("Medication with this Code already exists"));

        mockMvc.perform(post("/api/medication")
                        .contentType("application/json")
                        .content("{\"Code\":1,\"Name\":\"Ibuprofen\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllMedications() throws Exception {
        when(medicationService.findAll()).thenReturn(List.of(medication));

        mockMvc.perform(get("/api/medication"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Name").value("Ibuprofen"));
    }
}
