package com.hms.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(
        @NotNull(message = "is required") @JsonProperty("RoomNumber") Integer roomNumber,
        @NotNull(message = "is required") @JsonProperty("RoomType") String roomType,
        @NotNull(message = "is required") @JsonProperty("BlockFloor") Integer blockFloor,
        @NotNull(message = "is required") @JsonProperty("BlockCode") Integer blockCode,
        @JsonProperty("Unavailable") Integer unavailable) {
}
