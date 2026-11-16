package com.hms.hospital.dto.response;

import com.hms.hospital.entity.AppUser;

/** AppUser with the password field stripped out before it ever leaves the service layer. */
public record UserResponse(
        Integer id,
        String username,
        String role,
        String displayName,
        Long ssn,
        Integer employeeId) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(), user.getUsername(), user.getRole(),
                user.getDisplayName(), user.getSsn(), user.getEmployeeId());
    }
}
