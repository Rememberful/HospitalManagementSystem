package com.hms.hospital.security;

import com.hms.hospital.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Row-level ownership checks for use in @PreAuthorize expressions, e.g.
 * {@code @PreAuthorize("hasRole('ADMIN') or @securityUtil.isSelfPatient(#ssn)")}.
 * Role membership alone (checked via hasRole/hasAnyRole) says who can call an endpoint
 * shape at all; these say whether *this* caller may touch *this* particular record.
 */
@Component("securityUtil")
@RequiredArgsConstructor
public class SecurityUtil {

    private final AppointmentRepository appointmentRepository;

    public boolean isSelfPatient(Long ssn) {
        UserPrincipal principal = currentPrincipal();
        return principal != null && ssn != null && ssn.equals(principal.getSsn());
    }

    public boolean isSelfDoctor(Integer employeeId) {
        UserPrincipal principal = currentPrincipal();
        return principal != null && employeeId != null && employeeId.equals(principal.getEmployeeId());
    }

    public boolean isSelfNurse(Integer employeeId) {
        return isSelfDoctor(employeeId);
    }

    /** True if the given appointment belongs to the currently authenticated patient. */
    public boolean ownsAppointment(Long appointmentId) {
        UserPrincipal principal = currentPrincipal();
        if (principal == null || principal.getSsn() == null || appointmentId == null) return false;
        return appointmentRepository.findById(appointmentId)
                .map(a -> principal.getSsn().equals(a.getPatient()))
                .orElse(false);
    }

    private UserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) return null;
        return principal;
    }
}
