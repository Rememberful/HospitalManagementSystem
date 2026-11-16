package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AppointmentCreateRequest(
        @NotNull(message = "is required") @JsonProperty("AppointmentID") Long appointmentID,
        @NotNull(message = "is required") @JsonProperty("Patient") Long patient,
        @JsonProperty("PrepNurse") Integer prepNurse,
        @NotNull(message = "is required") @JsonProperty("Physician") Integer physician,
        @NotNull(message = "is required") @JsonProperty("Starto") String starto,
        @NotNull(message = "is required") @JsonProperty("Endo") String endo,
        @NotNull(message = "is required") @JsonProperty("ExaminationRoom") String examinationRoom,
        @JsonProperty("VisitType") String visitType) {
}
