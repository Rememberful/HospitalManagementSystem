package com.hms.hospital.controller;

import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.PhysicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PUT /update/position/{position}/{employeeId} is the one route in the original API that
 * wasn't namespaced under /api — kept as-is for compatibility.
 */
@RestController
@RequiredArgsConstructor
public class RootController {

    private final PhysicianService physicianService;

    @PutMapping("/update/position/{position}/{employeeId}")
    public Physician updatePosition(@PathVariable String position, @PathVariable Integer employeeId) {
        if (position == null || position.isBlank()) throw new BadRequestException("Position is required");
        return physicianService.updatePosition(employeeId, position);
    }
}
