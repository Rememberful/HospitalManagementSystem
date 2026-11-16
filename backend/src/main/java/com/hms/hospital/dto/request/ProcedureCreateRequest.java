package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ProcedureCreateRequest(
        @NotNull(message = "is required") @JsonProperty("Code") Integer code,
        @NotNull(message = "is required") @JsonProperty("Name") String name,
        @NotNull(message = "is required") @JsonProperty("Cost") Double cost) {
}
