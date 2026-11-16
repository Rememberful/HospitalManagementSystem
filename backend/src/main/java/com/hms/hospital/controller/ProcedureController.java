package com.hms.hospital.controller;

import com.hms.hospital.dto.request.ProcedureCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.ProcedureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/procedure")
@RequiredArgsConstructor
public class ProcedureController {

    private final ProcedureService procedureService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody ProcedureCreateRequest request) {
        procedureService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping({"", "/"})
    public List<MedicalProcedure> list() {
        return procedureService.findAll();
    }

    @GetMapping("/cost/{id}")
    public Map<String, Object> getCost(@PathVariable Integer id) {
        return Map.of("Code", id, "Cost", procedureService.getCost(id));
    }

    @PutMapping("/cost/{id}")
    public MedicalProcedure updateCost(@PathVariable Integer id, @RequestBody Map<String, Double> body) {
        Double cost = body.get("Cost");
        if (cost == null) throw new BadRequestException("Cost is required");
        return procedureService.updateCost(id, cost);
    }

    @PutMapping("/name/{id}")
    public MedicalProcedure updateName(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String name = body.get("Name");
        if (name == null || name.isBlank()) throw new BadRequestException("Name is required");
        return procedureService.updateName(id, name);
    }

    @GetMapping("/search/{name}")
    public List<MedicalProcedure> search(@PathVariable String name) {
        return procedureService.searchByName(name);
    }
}
