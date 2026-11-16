package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotNull;

public record SalaryUpsertRequest(
        @NotNull(message = "is required") Integer physician,
        @NotNull(message = "is required") Double baseSalary,
        @NotNull(message = "is required") Double commissionRate) {
}
