package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record PatientCreateRequest(
        @NotNull(message = "is required") @JsonProperty("SSN") Long ssn,
        @NotNull(message = "is required") @JsonProperty("Name") String name,
        @NotNull(message = "is required") @JsonProperty("Address") String address,
        @NotNull(message = "is required") @JsonProperty("Phone") String phone,
        @NotNull(message = "is required") @JsonProperty("InsuranceID") Long insuranceID,
        @NotNull(message = "is required") @JsonProperty("PCP") Integer pcp) {
}
