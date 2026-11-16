package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.HospitalStatsResponse;
import com.hms.hospital.entity.Billing;
import com.hms.hospital.entity.Room;
import com.hms.hospital.repository.*;
import com.hms.hospital.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final PatientRepository patientRepository;
    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillingRepository billingRepository;
    private final RoomRepository roomRepository;
    private final StayRepository stayRepository;

    @Override
    public HospitalStatsResponse getStats() {
        String today = LocalDate.now().toString(); // yyyy-MM-dd, matching the seeded date format's prefix

        long appointmentsToday = appointmentRepository.findAll().stream()
                .filter(a -> a.getStarto() != null && a.getStarto().startsWith(today))
                .count();

        double totalRevenue = billingRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getPaid()))
                .mapToDouble(Billing::getTotal)
                .sum();

        long unpaidInvoiceCount = billingRepository.findAll().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getPaid()))
                .count();

        long occupiedRooms = roomRepository.findAll().stream()
                .filter(r -> Integer.valueOf(1).equals(r.getUnavailable()))
                .count();

        long activeAdmissions = stayRepository.findAll().stream()
                .filter(s -> s.getStayEnd() == null || s.getStayEnd().isBlank())
                .count();

        return new HospitalStatsResponse(
                patientRepository.count(),
                physicianRepository.count(),
                nurseRepository.count(),
                departmentRepository.count(),
                appointmentsToday,
                appointmentRepository.count(),
                Math.round(totalRevenue * 100.0) / 100.0,
                unpaidInvoiceCount,
                (int) occupiedRooms,
                (int) roomRepository.count(),
                activeAdmissions);
    }
}
