package com.hms.hospital.controller;

import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/{resource}/{id}")
    public Object get(@PathVariable String resource, @PathVariable Long id) {
        return adminService.get(resource, id);
    }

    @PutMapping("/{resource}/{id}")
    public Object update(@PathVariable String resource, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return adminService.update(resource, id, body);
    }

    @DeleteMapping("/{resource}/{id}")
    public MessageResponse delete(@PathVariable String resource, @PathVariable Long id) {
        adminService.delete(resource, id);
        return new MessageResponse("Deleted successfully");
    }
}
