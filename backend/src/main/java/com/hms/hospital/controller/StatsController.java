package com.hms.hospital.controller;

import com.hms.hospital.dto.response.HospitalStatsResponse;
import com.hms.hospital.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public HospitalStatsResponse stats() {
        return statsService.getStats();
    }
}
