package com.hms.hospital.dto.response;

import com.hms.hospital.entity.DutyShift;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.entity.OnCall;

import java.util.List;

public record NurseDashboardResponse(
        Nurse profile,
        List<AssignmentView> assignments,
        List<OnCall> onCall,
        List<DutyShift> dutyShifts) {
}
