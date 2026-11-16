package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Every field optional — only non-null fields are patched onto the existing appointment. */
public record AppointmentUpdateRequest(
        @JsonProperty("Physician") Integer physician,
        @JsonProperty("Starto") String starto,
        @JsonProperty("Endo") String endo,
        @JsonProperty("ExaminationRoom") String examinationRoom,
        @JsonProperty("VisitType") String visitType) {
}
