package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotNull;

public record BlockCreateRequest(
        @NotNull(message = "is required") Integer blockFloor,
        @NotNull(message = "is required") Integer blockCode) {
}
