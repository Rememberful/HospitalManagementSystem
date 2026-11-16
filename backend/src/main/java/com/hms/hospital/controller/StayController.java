package com.hms.hospital.controller;

import com.hms.hospital.dto.request.StayAdmitRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Stay;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.StayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stay")
@RequiredArgsConstructor
public class StayController {

    private final StayService stayService;

    @PostMapping("/admit")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse admit(@Valid @RequestBody StayAdmitRequest request) {
        stayService.admit(request);
        return new MessageResponse("Patient admitted successfully");
    }

    @PutMapping("/discharge/{stayId}")
    public Stay discharge(@PathVariable Integer stayId, @RequestBody Map<String, String> body) {
        String stayEnd = body.get("StayEnd");
        if (stayEnd == null || stayEnd.isBlank()) throw new BadRequestException("StayEnd is required");
        return stayService.discharge(stayId, stayEnd);
    }

    @GetMapping
    public List<Stay> list() {
        return stayService.findAll();
    }
}
