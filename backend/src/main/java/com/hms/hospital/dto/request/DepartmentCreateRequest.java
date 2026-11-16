package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record DepartmentCreateRequest(
        @NotNull(message = "is required") @JsonProperty("DepartmentID") Integer departmentID,
        @NotNull(message = "is required") @JsonProperty("Name") String name,
        @NotNull(message = "is required") @JsonProperty("Head") Integer head) {
}
