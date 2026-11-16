package com.hms.hospital.dto.response;

import java.time.Instant;

/**
 * Matches the {timeStamp, message} shape the original json-server mock API returned,
 * so the existing React client needs no error-handling changes.
 */
public record ErrorResponse(Instant timeStamp, String message) {

    public ErrorResponse(String message) {
        this(Instant.now(), message);
    }
}
