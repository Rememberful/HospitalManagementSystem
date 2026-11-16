package com.hms.hospital.controller;

import com.hms.hospital.entity.Prescription;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.PrescriptionService;
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

@ControllerTest(PrescriptionController.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService prescriptionService;

    private final Prescription prescription = new Prescription("1", 3, 100000001L, 1, "2030-01-01", null, "1 tablet daily");

    @Test
    void createReturns201() throws Exception {
        when(prescriptionService.create(any())).thenReturn(prescription);

        mockMvc.perform(post("/api/prescribe")
                        .contentType("application/json")
                        .content("{\"physician\":3,\"patient\":100000001,\"medication\":1,\"date\":\"2030-01-01\",\"dose\":\"1 tablet daily\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createRejectsUnknownMedicationWith400() throws Exception {
        when(prescriptionService.create(any())).thenThrow(new BadRequestException("medication must reference an existing Medication"));

        mockMvc.perform(post("/api/prescribe")
                        .contentType("application/json")
                        .content("{\"physician\":3,\"patient\":100000001,\"medication\":999,\"date\":\"2030-01-01\",\"dose\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void byPatientReturnsList() throws Exception {
        when(prescriptionService.findByPatient(100000001L)).thenReturn(List.of(prescription));

        mockMvc.perform(get("/api/prescribe/patient/100000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dose").value("1 tablet daily"));
    }
}
