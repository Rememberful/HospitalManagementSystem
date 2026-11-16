package com.hms.hospital.dto.response;

public record HospitalStatsResponse(
        long totalPatients,
        long totalPhysicians,
        long totalNurses,
        long totalDepartments,
        long appointmentsToday,
        long totalAppointments,
        double totalRevenue,
        long unpaidInvoiceCount,
        int occupiedRooms,
        int totalRooms,
        long activeAdmissions) {
}
