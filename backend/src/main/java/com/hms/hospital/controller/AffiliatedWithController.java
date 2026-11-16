package com.hms.hospital.controller;

import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.service.AffiliatedWithService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/affiliated_with")
@RequiredArgsConstructor
public class AffiliatedWithController {

    private final AffiliatedWithService affiliatedWithService;

    @PostMapping("/post")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@RequestBody Map<String, Object> body) {
        Integer physician = (Integer) body.get("Physician");
        Integer department = (Integer) body.get("Department");
        Boolean primary = (Boolean) body.get("PrimaryAffiliation");
        affiliatedWithService.create(physician, department, primary);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping("/physicians/{deptid}")
    public List<Physician> physiciansForDepartment(@PathVariable Integer deptid) {
        return affiliatedWithService.getPhysiciansForDepartment(deptid);
    }

    @GetMapping("/department/{physicianid}")
    public List<Department> departmentsForPhysician(@PathVariable Integer physicianid) {
        return affiliatedWithService.getDepartmentsForPhysician(physicianid);
    }

    @GetMapping("/primary/{physicianid}")
    public Department primaryAffiliation(@PathVariable Integer physicianid) {
        return affiliatedWithService.getPrimaryAffiliation(physicianid);
    }
}
