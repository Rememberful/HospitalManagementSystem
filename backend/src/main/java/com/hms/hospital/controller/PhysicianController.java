package com.hms.hospital.controller;

import com.hms.hospital.dto.request.PhysicianCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.PhysicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/physician")
@RequiredArgsConstructor
public class PhysicianController {

    private final PhysicianService physicianService;

    @PostMapping("/post")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody PhysicianCreateRequest request) {
        physicianService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping("/empid/{empid}")
    public Physician getByEmployeeId(@PathVariable Integer empid) {
        return physicianService.getByEmployeeId(empid);
    }

    @GetMapping("/name/{name}")
    public Physician getByName(@PathVariable String name) {
        return physicianService.getByName(name);
    }

    @GetMapping("/position/{pos}")
    public List<Physician> getByPosition(@PathVariable String pos) {
        return physicianService.getByPosition(pos);
    }

    @PutMapping("/update/name/{empid}")
    public Physician updateName(@PathVariable Integer empid, @RequestBody Map<String, String> body) {
        String name = body.get("Name");
        if (name == null || name.isBlank()) throw new BadRequestException("Name is required");
        return physicianService.updateName(empid, name);
    }

    @PutMapping("/update/ssn/{empid}")
    public Physician updateSsn(@PathVariable Integer empid, @RequestBody Map<String, Long> body) {
        Long ssn = body.get("SSN");
        if (ssn == null) throw new BadRequestException("SSN is required");
        return physicianService.updateSsn(empid, ssn);
    }
}
