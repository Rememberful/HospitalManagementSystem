package com.hms.hospital.controller;

import com.hms.hospital.dto.request.OnCallCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.OnCall;
import com.hms.hospital.service.OnCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/on_call")
@RequiredArgsConstructor
public class OnCallController {

    private final OnCallService onCallService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody OnCallCreateRequest request) {
        onCallService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping
    public List<OnCall> list() {
        return onCallService.findAll();
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable String id) {
        onCallService.delete(id);
        return new MessageResponse("Deleted successfully");
    }
}
