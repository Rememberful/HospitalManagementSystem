package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.BillingCreateRequest;
import com.hms.hospital.entity.Billing;
import com.hms.hospital.entity.BillingItem;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.BillingRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.StayRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock private BillingRepository billingRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private StayRepository stayRepository;

    @InjectMocks
    private BillingServiceImpl billingService;

    @Test
    @DisplayName("create computes the total from the line items and starts unpaid")
    void createsInvoiceComputingTotal() {
        List<BillingItem> items = List.of(
                new BillingItem("procedure", "Consultation", 150.0),
                new BillingItem("room", "Single room, 3 nights", 900.0));
        BillingCreateRequest request = new BillingCreateRequest(100000001L, null, items);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(billingRepository.save(any(Billing.class))).thenAnswer(inv -> inv.getArgument(0));

        Billing result = billingService.create(request);

        assertThat(result.getTotal()).isEqualTo(1050.0);
        assertThat(result.getPaid()).isFalse();
    }

    @Test
    @DisplayName("create rounds the computed total to two decimal places")
    void roundsTotalToTwoDecimals() {
        List<BillingItem> items = List.of(new BillingItem("procedure", "X", 33.333));
        BillingCreateRequest request = new BillingCreateRequest(100000001L, null, items);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(billingRepository.save(any(Billing.class))).thenAnswer(inv -> inv.getArgument(0));

        Billing result = billingService.create(request);

        assertThat(result.getTotal()).isEqualTo(33.33);
    }

    @Test
    @DisplayName("create rejects an unknown patient")
    void rejectsUnknownPatient() {
        BillingCreateRequest request = new BillingCreateRequest(999L, null, List.of(new BillingItem("x", "x", 1.0)));
        when(patientRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> billingService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create rejects an unknown stay reference when one is supplied")
    void rejectsUnknownStay() {
        BillingCreateRequest request = new BillingCreateRequest(100000001L, 404, List.of(new BillingItem("x", "x", 1.0)));
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(stayRepository.existsById(404)).thenReturn(false);

        assertThatThrownBy(() -> billingService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("stayId must reference");
    }

    @Test
    @DisplayName("markPaid flips the paid flag")
    void marksPaid() {
        Billing billing = new Billing("b1", 100000001L, null, List.of(), 100.0, false);
        when(billingRepository.findById("b1")).thenReturn(Optional.of(billing));
        when(billingRepository.save(any(Billing.class))).thenAnswer(inv -> inv.getArgument(0));

        Billing result = billingService.markPaid("b1");

        assertThat(result.getPaid()).isTrue();
    }

    @Test
    @DisplayName("markPaid throws ResourceNotFoundException for an unknown invoice")
    void markPaidMissing() {
        when(billingRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.markPaid("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
