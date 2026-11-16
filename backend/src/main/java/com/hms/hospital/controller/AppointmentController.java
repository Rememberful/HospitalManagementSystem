package com.hms.hospital.controller;

import com.hms.hospital.dto.request.AppointmentCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(#request.patient())")
    public MessageResponse book(@Valid @RequestBody AppointmentCreateRequest request) {
        appointmentService.book(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping({"", "/"})
    public List<Appointment> list() {
        return appointmentService.findAll();
    }

    /**
     * Single-appointment lookup. Ownership can only be decided once the record is loaded (we need
     * its Patient/Physician), so this uses @PostAuthorize against the returned object: admins see
     * anything, a patient sees only their own appointment, a doctor only ones they're assigned to.
     */
    @GetMapping("/{id}")
    @PostAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(returnObject.patient) "
            + "or @securityUtil.isSelfDoctor(returnObject.physician)")
    public Appointment getById(@PathVariable Long id) {
        return appointmentService.getById(id);
    }
}
