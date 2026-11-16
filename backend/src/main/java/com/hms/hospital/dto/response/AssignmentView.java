package com.hms.hospital.dto.response;

import com.hms.hospital.entity.Patient;
import com.hms.hospital.entity.Physician;

/** One unit of a nurse's workload: either a prep assignment or an operation-assist assignment. */
public record AssignmentView(
        String type, Patient patient, Physician supervisingDoctor, String room, String when) {
}
