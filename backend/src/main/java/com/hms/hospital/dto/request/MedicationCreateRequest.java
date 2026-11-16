package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record MedicationCreateRequest(
        @NotNull(message = "is required") @JsonProperty("Code") Integer code,
        @NotNull(message = "is required") @JsonProperty("Name") String name,
        @JsonProperty("Brand") String brand,
        @JsonProperty("Description") String description) {
}
