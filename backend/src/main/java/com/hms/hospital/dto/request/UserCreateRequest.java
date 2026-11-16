package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Admin creates a login account for a staff member or patient. */
public record UserCreateRequest(
        @NotNull(message = "is required") Integer id,
        @NotBlank(message = "is required") String username,
        @NotBlank(message = "is required") String password,
        @NotBlank(message = "is required") String role,
        @NotBlank(message = "is required") String displayName,
        Long ssn,
        Integer employeeId) {
}
