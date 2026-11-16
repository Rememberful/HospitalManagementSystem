package com.hms.hospital.controller;

import com.hms.hospital.dto.request.MedicationCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Medication;
import com.hms.hospital.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medication")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody MedicationCreateRequest request) {
        medicationService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping
    public List<Medication> list() {
        return medicationService.findAll();
    }
}
