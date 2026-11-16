package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record NurseCreateRequest(
        @NotNull(message = "is required") @JsonProperty("EmployeeID") Integer employeeID,
        @NotNull(message = "is required") @JsonProperty("Name") String name,
        @NotNull(message = "is required") @JsonProperty("Position") String position,
        @NotNull(message = "is required") @JsonProperty("Registered") Integer registered,
        @NotNull(message = "is required") @JsonProperty("SSN") Long ssn) {
}
