package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotNull;

public record OnCallCreateRequest(
        @NotNull(message = "is required") Integer nurse,
        @NotNull(message = "is required") Integer blockFloor,
        @NotNull(message = "is required") Integer blockCode,
        @NotNull(message = "is required") String onCallStart,
        @NotNull(message = "is required") String onCallEnd) {
}
