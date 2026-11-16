package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotNull;

public record StayAdmitRequest(
        @NotNull(message = "is required") Integer stayId,
        @NotNull(message = "is required") Long patient,
        @NotNull(message = "is required") Integer room,
        @NotNull(message = "is required") String stayStart) {
}
