package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "is required") String username,
        @NotBlank(message = "is required") String password) {
}
