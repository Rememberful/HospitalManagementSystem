package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotNull;

public record PrescriptionCreateRequest(
        @NotNull(message = "is required") Integer physician,
        @NotNull(message = "is required") Long patient,
        @NotNull(message = "is required") Integer medication,
        @NotNull(message = "is required") String date,
        Long appointment,
        @NotNull(message = "is required") String dose) {
}
