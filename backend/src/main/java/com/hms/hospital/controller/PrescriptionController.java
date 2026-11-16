package com.hms.hospital.controller;

import com.hms.hospital.dto.request.PrescriptionCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Prescription;
import com.hms.hospital.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescribe")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody PrescriptionCreateRequest request) {
        prescriptionService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping
    public List<Prescription> list() {
        return prescriptionService.findAll();
    }

    @GetMapping("/patient/{ssn}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or @securityUtil.isSelfPatient(#ssn)")
    public List<Prescription> byPatient(@PathVariable Long ssn) {
        return prescriptionService.findByPatient(ssn);
    }
}
