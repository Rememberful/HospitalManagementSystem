package com.hms.hospital.controller;

import com.hms.hospital.dto.request.NurseCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.NurseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nurse")
@RequiredArgsConstructor
public class NurseController {

    private final NurseService nurseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody NurseCreateRequest request) {
        nurseService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping({"", "/"})
    public List<Nurse> list() {
        return nurseService.findAll();
    }

    @GetMapping("/empid/{empid}")
    public Nurse getByEmployeeId(@PathVariable Integer empid) {
        return nurseService.getByEmployeeId(empid);
    }

    @GetMapping("/position/{empid}")
    public Map<String, String> getPosition(@PathVariable Integer empid) {
        return Map.of("Position", nurseService.getPosition(empid));
    }

    @GetMapping("/registered/{empid}")
    public Map<String, Boolean> isRegistered(@PathVariable Integer empid) {
        return Map.of("Registered", nurseService.isRegistered(empid));
    }

    @PutMapping("/registered/{empid}")
    public Nurse updateRegistered(@PathVariable Integer empid, @RequestBody Map<String, Integer> body) {
        Integer registered = body.get("Registered");
        if (registered == null) throw new BadRequestException("Registered is required");
        return nurseService.updateRegistered(empid, registered);
    }

    @PutMapping("/ssn/{empid}")
    public Nurse updateSsn(@PathVariable Integer empid, @RequestBody Map<String, Long> body) {
        Long ssn = body.get("SSN");
        if (ssn == null) throw new BadRequestException("SSN is required");
        return nurseService.updateSsn(empid, ssn);
    }
}
