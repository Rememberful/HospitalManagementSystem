package com.hms.hospital.controller;

import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.dto.response.TrainedInWithProcedureView;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.TrainedIn;
import com.hms.hospital.service.TrainedInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trained_in")
@RequiredArgsConstructor
public class TrainedInController {

    private final TrainedInService trainedInService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@RequestBody Map<String, Object> body) {
        trainedInService.create(
                (Integer) body.get("Physician"),
                (Integer) body.get("Treatment"),
                (String) body.get("CertificationDate"),
                (String) body.get("CertificationExpires"));
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping({"", "/"})
    public List<TrainedIn> list() {
        return trainedInService.findAll();
    }

    @GetMapping("/treatment/{physicianid}")
    public List<TrainedInWithProcedureView> treatmentsForPhysician(@PathVariable Integer physicianid) {
        return trainedInService.treatmentsForPhysician(physicianid);
    }

    @GetMapping("/physicians/{procedureid}")
    public List<Physician> physiciansForProcedure(@PathVariable Integer procedureid) {
        return trainedInService.physiciansForProcedure(procedureid);
    }
}
