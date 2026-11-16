package com.hms.hospital.dto.response;

public record SalaryView(
        Integer physician, Double baseSalary, Double commissionRate,
        Double commissionEarned, Double totalCompensation) {
}
