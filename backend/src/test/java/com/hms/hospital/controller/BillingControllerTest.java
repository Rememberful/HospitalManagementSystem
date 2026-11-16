package com.hms.hospital.controller;

import com.hms.hospital.entity.Billing;
import com.hms.hospital.entity.BillingItem;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.BillingService;
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

@ControllerTest(BillingController.class)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    private final Billing invoice = new Billing("b1", 100000001L, null,
            List.of(new BillingItem("procedure", "Consultation", 150.0)), 150.0, false);

    @Test
    void createReturns201WithComputedTotal() throws Exception {
        when(billingService.create(any())).thenReturn(invoice);

        mockMvc.perform(post("/api/billing")
                        .contentType("application/json")
                        .content("{\"patient\":100000001,\"items\":[{\"type\":\"procedure\",\"label\":\"Consultation\",\"amount\":150.0}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(150.0))
                .andExpect(jsonPath("$.paid").value(false));
    }

    @Test
    void createWithNoLineItemsReturns400() throws Exception {
        mockMvc.perform(post("/api/billing")
                        .contentType("application/json")
                        .content("{\"patient\":100000001,\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsUnknownPatientWith400() throws Exception {
        when(billingService.create(any())).thenThrow(new BadRequestException("patient must reference an existing Patient"));

        mockMvc.perform(post("/api/billing")
                        .contentType("application/json")
                        .content("{\"patient\":999,\"items\":[{\"type\":\"x\",\"label\":\"x\",\"amount\":1.0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markPaidReturns200() throws Exception {
        Billing paid = new Billing("b1", 100000001L, null, List.of(), 150.0, true);
        when(billingService.markPaid(eq("b1"))).thenReturn(paid);

        mockMvc.perform(put("/api/billing/b1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true));
    }

    @Test
    void markPaidMissingInvoiceReturns404() throws Exception {
        when(billingService.markPaid(eq("nope"))).thenThrow(new ResourceNotFoundException("Billing"));

        mockMvc.perform(put("/api/billing/nope/pay"))
                .andExpect(status().isNotFound());
    }

    @Test
    void byPatientReturnsList() throws Exception {
        when(billingService.findByPatient(100000001L)).thenReturn(List.of(invoice));

        mockMvc.perform(get("/api/billing/patient/100000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("b1"));
    }
}
