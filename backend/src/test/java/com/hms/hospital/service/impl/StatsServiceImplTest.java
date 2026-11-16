package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.HospitalStatsResponse;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Billing;
import com.hms.hospital.entity.Room;
import com.hms.hospital.entity.Stay;
import com.hms.hospital.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock private PatientRepository patientRepository;
    @Mock private PhysicianRepository physicianRepository;
    @Mock private NurseRepository nurseRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private BillingRepository billingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private StayRepository stayRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    @DisplayName("counts simple totals directly from repository counts")
    void countsSimpleTotals() {
        when(patientRepository.count()).thenReturn(5L);
        when(physicianRepository.count()).thenReturn(9L);
        when(nurseRepository.count()).thenReturn(3L);
        when(departmentRepository.count()).thenReturn(3L);
        when(appointmentRepository.count()).thenReturn(8L);
        when(appointmentRepository.findAll()).thenReturn(List.of());
        when(billingRepository.findAll()).thenReturn(List.of());
        when(roomRepository.findAll()).thenReturn(List.of());
        when(roomRepository.count()).thenReturn(12L);
        when(stayRepository.findAll()).thenReturn(List.of());

        HospitalStatsResponse result = statsService.getStats();

        assertThat(result.totalPatients()).isEqualTo(5);
        assertThat(result.totalPhysicians()).isEqualTo(9);
        assertThat(result.totalNurses()).isEqualTo(3);
        assertThat(result.totalDepartments()).isEqualTo(3);
        assertThat(result.totalAppointments()).isEqualTo(8);
        assertThat(result.totalRooms()).isEqualTo(12);
    }

    @Test
    @DisplayName("counts only appointments starting today as appointmentsToday")
    void countsAppointmentsToday() {
        String today = LocalDate.now().toString();
        Appointment todayAppt = new Appointment(1L, 100000001L, 101, 1, today + " 09:00", today + " 09:30", "A", "OPD");
        Appointment pastAppt = new Appointment(2L, 100000002L, 101, 1, "2008-01-01 09:00", "2008-01-01 09:30", "B", "OPD");
        setZeroDefaults();
        when(appointmentRepository.findAll()).thenReturn(List.of(todayAppt, pastAppt));

        HospitalStatsResponse result = statsService.getStats();

        assertThat(result.appointmentsToday()).isEqualTo(1);
    }

    @Test
    @DisplayName("sums only paid invoices into totalRevenue, and counts the rest as unpaid")
    void computesRevenueAndUnpaidCount() {
        Billing paid = new Billing("b1", 100000001L, null, List.of(), 500.0, true);
        Billing unpaid = new Billing("b2", 100000001L, null, List.of(), 250.0, false);
        setZeroDefaults();
        when(billingRepository.findAll()).thenReturn(List.of(paid, unpaid));

        HospitalStatsResponse result = statsService.getStats();

        assertThat(result.totalRevenue()).isEqualTo(500.0);
        assertThat(result.unpaidInvoiceCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("counts unavailable rooms as occupied")
    void countsOccupiedRooms() {
        Room occupied = new Room(101, "Single", 1, 1, 1);
        Room free = new Room(102, "Single", 1, 1, 0);
        setZeroDefaults();
        when(roomRepository.findAll()).thenReturn(List.of(occupied, free));

        HospitalStatsResponse result = statsService.getStats();

        assertThat(result.occupiedRooms()).isEqualTo(1);
    }

    @Test
    @DisplayName("counts stays with no StayEnd as active admissions")
    void countsActiveAdmissions() {
        Stay active = new Stay(1, 100000001L, 101, "2030-01-01", null);
        Stay discharged = new Stay(2, 100000002L, 102, "2030-01-01", "2030-01-05");
        setZeroDefaults();
        when(stayRepository.findAll()).thenReturn(List.of(active, discharged));

        HospitalStatsResponse result = statsService.getStats();

        assertThat(result.activeAdmissions()).isEqualTo(1);
    }

    private void setZeroDefaults() {
        when(appointmentRepository.findAll()).thenReturn(List.of());
        when(billingRepository.findAll()).thenReturn(List.of());
        when(roomRepository.findAll()).thenReturn(List.of());
        when(stayRepository.findAll()).thenReturn(List.of());
    }
}
