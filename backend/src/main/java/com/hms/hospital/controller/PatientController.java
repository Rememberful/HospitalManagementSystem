package com.hms.hospital.controller;

import com.hms.hospital.dto.request.AppointmentUpdateRequest;
import com.hms.hospital.dto.request.PatientCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.AppointmentService;
import com.hms.hospital.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody PatientCreateRequest request) {
        patientService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping({"", "/"})
    public List<Patient> list() {
        return patientService.findAll();
    }

    /** Single patient by SSN — the patient themself, or clinical staff/admin looking up a specific patient. */
    @GetMapping("/{ssn}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE') or @securityUtil.isSelfPatient(#ssn)")
    public Patient getBySsn(@PathVariable Long ssn) {
        return patientService.getBySsn(ssn);
    }

    @GetMapping("/physician/{physicianid}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfDoctor(#physicianid)")
    public List<Patient> getByPhysician(@PathVariable Integer physicianid) {
        return patientService.getByPhysician(physicianid);
    }

    @GetMapping("/physician/{physicianid}/{patientid}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfDoctor(#physicianid)")
    public Patient getByPhysicianAndPatient(@PathVariable Integer physicianid, @PathVariable Long patientid) {
        return patientService.getByPhysicianAndSsn(physicianid, patientid);
    }

    @GetMapping("/insurance/{patientid}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(#patientid)")
    public Map<String, Long> getInsurance(@PathVariable Long patientid) {
        return Map.of("InsuranceID", patientService.getInsuranceId(patientid));
    }

    @PutMapping("/address/{ssn}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(#ssn)")
    public Patient updateAddress(@PathVariable Long ssn, @RequestBody Map<String, String> body) {
        String address = body.get("Address");
        if (address == null || address.isBlank()) throw new BadRequestException("Address is required");
        return patientService.updateAddress(ssn, address);
    }

    @PutMapping("/phone/{ssn}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(#ssn)")
    public Patient updatePhone(@PathVariable Long ssn, @RequestBody Map<String, String> body) {
        String phone = body.get("Phone");
        if (phone == null || phone.isBlank()) throw new BadRequestException("Phone is required");
        return patientService.updatePhone(ssn, phone);
    }

    @PutMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.ownsAppointment(#appointmentId)")
    public Appointment editOwnAppointment(@PathVariable Long appointmentId, @RequestBody AppointmentUpdateRequest request) {
        return appointmentService.update(appointmentId, request);
    }
}
