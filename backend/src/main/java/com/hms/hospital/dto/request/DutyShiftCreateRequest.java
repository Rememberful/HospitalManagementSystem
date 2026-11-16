package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotNull;

public record DutyShiftCreateRequest(
        @NotNull(message = "is required") String personType,
        @NotNull(message = "is required") Integer personId,
        @NotNull(message = "is required") String date,
        @NotNull(message = "is required") String joining,
        @NotNull(message = "is required") String leaving,
        String shiftType) {
}
