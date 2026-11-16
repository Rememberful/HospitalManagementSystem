package com.hms.hospital.dto.response;

public record LoginResponse(String token, UserResponse user) {
}
