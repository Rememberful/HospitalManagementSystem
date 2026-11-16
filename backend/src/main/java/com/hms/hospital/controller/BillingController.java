package com.hms.hospital.controller;

import com.hms.hospital.dto.request.BillingCreateRequest;
import com.hms.hospital.entity.Billing;
import com.hms.hospital.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Billing create(@Valid @RequestBody BillingCreateRequest request) {
        return billingService.create(request);
    }

    @PutMapping("/{id}/pay")
    public Billing markPaid(@PathVariable String id) {
        return billingService.markPaid(id);
    }

    @GetMapping
    public List<Billing> list() {
        return billingService.findAll();
    }

    @GetMapping("/patient/{ssn}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(#ssn)")
    public List<Billing> byPatient(@PathVariable Long ssn) {
        return billingService.findByPatient(ssn);
    }
}
