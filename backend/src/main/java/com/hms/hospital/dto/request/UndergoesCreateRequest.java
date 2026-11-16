package com.hms.hospital.dto.request;

import jakarta.validation.constraints.NotNull;

public record UndergoesCreateRequest(
        @NotNull(message = "is required") Long patient,
        @NotNull(message = "is required") Integer procedures,
        Integer stay,
        @NotNull(message = "is required") String dateUndergoes,
        @NotNull(message = "is required") Integer physician,
        Integer assistingNurse) {
}
