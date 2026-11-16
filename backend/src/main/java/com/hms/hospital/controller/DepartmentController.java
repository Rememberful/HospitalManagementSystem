package com.hms.hospital.controller;

import com.hms.hospital.dto.request.DepartmentCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody DepartmentCreateRequest request) {
        departmentService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping({"", "/"})
    public List<Department> list() {
        return departmentService.findAll();
    }

    @GetMapping("/{deptid}")
    public Department getById(@PathVariable Integer deptid) {
        return departmentService.getById(deptid);
    }

    @GetMapping("/head/{deptid}")
    public Physician getHead(@PathVariable Integer deptid) {
        return departmentService.getHead(deptid);
    }

    @GetMapping("/check/{physicianid}")
    public boolean isHead(@PathVariable Integer physicianid) {
        return departmentService.isPhysicianAHead(physicianid);
    }

    @PutMapping("/update/headid/{deptid}")
    public Department updateHead(@PathVariable Integer deptid, @RequestBody Map<String, Integer> body) {
        Integer head = body.get("Head");
        if (head == null) throw new BadRequestException("Head is required");
        return departmentService.updateHead(deptid, head);
    }
}
