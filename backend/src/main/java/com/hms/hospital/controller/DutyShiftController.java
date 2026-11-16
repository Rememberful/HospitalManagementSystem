package com.hms.hospital.controller;

import com.hms.hospital.dto.request.DutyShiftCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.DutyShift;
import com.hms.hospital.service.DutyShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duty_shift")
@RequiredArgsConstructor
public class DutyShiftController {

    private final DutyShiftService dutyShiftService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody DutyShiftCreateRequest request) {
        dutyShiftService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping
    public List<DutyShift> list() {
        return dutyShiftService.findAll();
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable String id) {
        dutyShiftService.delete(id);
        return new MessageResponse("Deleted successfully");
    }
}
