package com.hms.hospital.controller;

import com.hms.hospital.dto.response.TrainedInWithProcedureView;
import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.TrainedIn;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.TrainedInService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(TrainedInController.class)
class TrainedInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainedInService trainedInService;

    private final TrainedIn cert = new TrainedIn("3-1", 3, 1, "2008-01-01", "2008-12-31");
    private final Physician physician = new Physician(3, "Christopher Turk", "Surgeon", 333333333L);

    @Test
    void postCreatesCertificationAndReturns201() throws Exception {
        when(trainedInService.create(eq(3), eq(1), eq("2008-01-01"), eq("2008-12-31"))).thenReturn(cert);

        mockMvc.perform(post("/api/trained_in")
                        .contentType("application/json")
                        .content("{\"Physician\":3,\"Treatment\":1,\"CertificationDate\":\"2008-01-01\",\"CertificationExpires\":\"2008-12-31\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void postRejectsUnknownPhysicianWith400() throws Exception {
        when(trainedInService.create(eq(999), anyInt(), anyString(), anyString()))
                .thenThrow(new BadRequestException("Physician must reference an existing Physician"));

        mockMvc.perform(post("/api/trained_in")
                        .contentType("application/json")
                        .content("{\"Physician\":999,\"Treatment\":1,\"CertificationDate\":\"2008-01-01\",\"CertificationExpires\":\"2008-12-31\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllCertifications() throws Exception {
        when(trainedInService.findAll()).thenReturn(List.of(cert));

        mockMvc.perform(get("/api/trained_in/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3-1"));
    }

    @Test
    void treatmentsForPhysicianReturnsEnrichedList() throws Exception {
        MedicalProcedure proc = new MedicalProcedure(1, "Reverse Rhinopodoplasty", 1500.0);
        when(trainedInService.treatmentsForPhysician(3))
                .thenReturn(List.of(TrainedInWithProcedureView.from(cert, proc)));

        mockMvc.perform(get("/api/trained_in/treatment/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].procedure.Name").value("Reverse Rhinopodoplasty"));
    }

    @Test
    void physiciansForProcedureReturnsList() throws Exception {
        when(trainedInService.physiciansForProcedure(1)).thenReturn(List.of(physician));

        mockMvc.perform(get("/api/trained_in/physicians/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].EmployeeID").value(3));
    }
}
