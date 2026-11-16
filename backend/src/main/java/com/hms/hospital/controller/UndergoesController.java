package com.hms.hospital.controller;

import com.hms.hospital.dto.request.UndergoesCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Undergoes;
import com.hms.hospital.service.UndergoesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/undergoes")
@RequiredArgsConstructor
public class UndergoesController {

    private final UndergoesService undergoesService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse record(@Valid @RequestBody UndergoesCreateRequest request) {
        undergoesService.record(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping
    public List<Undergoes> list() {
        return undergoesService.findAll();
    }
}
