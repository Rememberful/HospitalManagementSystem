package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.AssignmentView;
import com.hms.hospital.dto.response.DoctorDashboardResponse;
import com.hms.hospital.dto.response.NurseDashboardResponse;
import com.hms.hospital.dto.response.OperationView;
import com.hms.hospital.dto.response.PatientDashboardResponse;
import com.hms.hospital.entity.*;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private PatientRepository patientRepository;
    @Mock private PhysicianRepository physicianRepository;
    @Mock private NurseRepository nurseRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private StayRepository stayRepository;
    @Mock private BillingRepository billingRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private UndergoesRepository undergoesRepository;
    @Mock private DutyShiftRepository dutyShiftRepository;
    @Mock private SalaryRepository salaryRepository;
    @Mock private MedicalProcedureRepository procedureRepository;
    @Mock private OnCallRepository onCallRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Nested
    @DisplayName("patientDashboard")
    class PatientDashboard {

        private final Patient patient = new Patient(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);

        @Test
        @DisplayName("assembles profile, PCP, appointments, stays, billing and prescriptions")
        void assemblesFullDashboard() {
            Physician pcp = new Physician(1, "John Dorian", "Staff Internist", 111111111L);
            when(patientRepository.findById(100000001L)).thenReturn(Optional.of(patient));
            when(physicianRepository.findById(1)).thenReturn(Optional.of(pcp));
            when(appointmentRepository.findByPatient(100000001L)).thenReturn(List.of());
            when(stayRepository.findByPatient(100000001L)).thenReturn(List.of());
            when(billingRepository.findByPatient(100000001L)).thenReturn(List.of());
            when(prescriptionRepository.findByPatient(100000001L)).thenReturn(List.of());

            PatientDashboardResponse result = dashboardService.patientDashboard(100000001L);

            assertThat(result.profile()).isEqualTo(patient);
            assertThat(result.primaryCarePhysician()).isEqualTo(pcp);
        }

        @Test
        @DisplayName("leaves primaryCarePhysician null when the patient has no PCP set")
        void noPcpSet() {
            Patient noPcp = new Patient(100000002L, "Grace Ritchie", "37 Snafu Drive", "555-0512", 36546321L, null);
            when(patientRepository.findById(100000002L)).thenReturn(Optional.of(noPcp));
            when(appointmentRepository.findByPatient(100000002L)).thenReturn(List.of());
            when(stayRepository.findByPatient(100000002L)).thenReturn(List.of());
            when(billingRepository.findByPatient(100000002L)).thenReturn(List.of());
            when(prescriptionRepository.findByPatient(100000002L)).thenReturn(List.of());

            PatientDashboardResponse result = dashboardService.patientDashboard(100000002L);

            assertThat(result.primaryCarePhysician()).isNull();
        }

        @Test
        @DisplayName("tolerates a PCP reference that no longer resolves to a physician")
        void danglingPcpReference() {
            when(patientRepository.findById(100000001L)).thenReturn(Optional.of(patient));
            when(physicianRepository.findById(1)).thenReturn(Optional.empty());
            when(appointmentRepository.findByPatient(100000001L)).thenReturn(List.of());
            when(stayRepository.findByPatient(100000001L)).thenReturn(List.of());
            when(billingRepository.findByPatient(100000001L)).thenReturn(List.of());
            when(prescriptionRepository.findByPatient(100000001L)).thenReturn(List.of());

            PatientDashboardResponse result = dashboardService.patientDashboard(100000001L);

            assertThat(result.primaryCarePhysician()).isNull();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for an unknown SSN")
        void throwsWhenPatientMissing() {
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dashboardService.patientDashboard(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("doctorDashboard")
    class DoctorDashboard {

        private final Physician physician = new Physician(3, "Christopher Turk", "Surgeon", 333333333L);

        @Test
        @DisplayName("throws ResourceNotFoundException for an unknown EmployeeID")
        void throwsWhenPhysicianMissing() {
            when(physicianRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dashboardService.doctorDashboard(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("computes commission and total compensation from procedure costs when a salary record exists")
        void computesCommission() {
            Appointment appt = new Appointment(1L, 100000001L, 101, 3, "s", "e", "A", "OPD");
            Undergoes op = new Undergoes("u1", 100000001L, 6, 3215, "2008-05-02", 3, 101);
            MedicalProcedure proc = new MedicalProcedure(6, "Obfuscated Dermogastrotomy", 4899.0);
            Salary salary = new Salary(3, 9500.0, 0.04);

            when(physicianRepository.findById(3)).thenReturn(Optional.of(physician));
            when(appointmentRepository.findByPhysician(3)).thenReturn(List.of(appt));
            when(undergoesRepository.findByPhysician(3)).thenReturn(List.of(op));
            when(dutyShiftRepository.findByPersonTypeAndPersonId("physician", 3)).thenReturn(List.of());
            when(salaryRepository.findByPhysician(3)).thenReturn(Optional.of(salary));
            when(procedureRepository.findAll()).thenReturn(List.of(proc));
            when(patientRepository.findBySsnIn(anyList())).thenReturn(List.of());

            DoctorDashboardResponse result = dashboardService.doctorDashboard(3);

            assertThat(result.operations()).hasSize(1);
            OperationView opView = result.operations().get(0);
            assertThat(opView.cost()).isEqualTo(4899.0);
            assertThat(opView.procedureName()).isEqualTo("Obfuscated Dermogastrotomy");

            // commissionEarned = round(4899 * 0.04, 2) = 195.96 ; totalCompensation = 9500 + 195.96
            assertThat(result.salary().commissionEarned()).isEqualTo(195.96);
            assertThat(result.salary().totalCompensation()).isEqualTo(9695.96);
        }

        @Test
        @DisplayName("returns a null salary block when the physician has no salary record")
        void noSalaryRecord() {
            when(physicianRepository.findById(3)).thenReturn(Optional.of(physician));
            when(appointmentRepository.findByPhysician(3)).thenReturn(List.of());
            when(undergoesRepository.findByPhysician(3)).thenReturn(List.of());
            when(dutyShiftRepository.findByPersonTypeAndPersonId("physician", 3)).thenReturn(List.of());
            when(salaryRepository.findByPhysician(3)).thenReturn(Optional.empty());
            when(procedureRepository.findAll()).thenReturn(List.of());
            when(patientRepository.findBySsnIn(anyList())).thenReturn(List.of());

            DoctorDashboardResponse result = dashboardService.doctorDashboard(3);

            assertThat(result.salary()).isNull();
        }

        @Test
        @DisplayName("treats an operation whose procedure no longer exists as zero cost, not an error")
        void danglingProcedureReferenceCostsZero() {
            Undergoes op = new Undergoes("u9", 100000001L, 404, 3215, "2008-05-02", 3, 101);
            when(physicianRepository.findById(3)).thenReturn(Optional.of(physician));
            when(appointmentRepository.findByPhysician(3)).thenReturn(List.of());
            when(undergoesRepository.findByPhysician(3)).thenReturn(List.of(op));
            when(dutyShiftRepository.findByPersonTypeAndPersonId("physician", 3)).thenReturn(List.of());
            when(salaryRepository.findByPhysician(3)).thenReturn(Optional.empty());
            when(procedureRepository.findAll()).thenReturn(List.of());
            when(patientRepository.findBySsnIn(anyList())).thenReturn(List.of());

            DoctorDashboardResponse result = dashboardService.doctorDashboard(3);

            assertThat(result.operations().get(0).cost()).isEqualTo(0.0);
            assertThat(result.operations().get(0).procedureName()).isNull();
        }

        @Test
        @DisplayName("counts every non-Admission appointment as a general checkup")
        void countsGeneralCheckups() {
            Appointment opd = new Appointment(1L, 100000001L, 101, 3, "s", "e", "A", "OPD");
            Appointment admission = new Appointment(2L, 100000002L, 101, 3, "s", "e", "B", "Admission");
            when(physicianRepository.findById(3)).thenReturn(Optional.of(physician));
            when(appointmentRepository.findByPhysician(3)).thenReturn(List.of(opd, admission));
            when(undergoesRepository.findByPhysician(3)).thenReturn(List.of());
            when(dutyShiftRepository.findByPersonTypeAndPersonId("physician", 3)).thenReturn(List.of());
            when(salaryRepository.findByPhysician(3)).thenReturn(Optional.empty());
            when(procedureRepository.findAll()).thenReturn(List.of());
            when(patientRepository.findBySsnIn(anyList())).thenReturn(List.of());

            DoctorDashboardResponse result = dashboardService.doctorDashboard(3);

            assertThat(result.generalCheckups()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("nurseDashboard")
    class NurseDashboardTests {

        private final Nurse nurse = new Nurse(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);
        private final Patient patient = new Patient(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);
        private final Physician physician = new Physician(3, "Christopher Turk", "Surgeon", 333333333L);

        @Test
        @DisplayName("throws ResourceNotFoundException for an unknown EmployeeID")
        void throwsWhenNurseMissing() {
            when(nurseRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dashboardService.nurseDashboard(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("turns a prep appointment into an Appointment-type assignment")
        void appointmentAssignment() {
            Appointment appt = new Appointment(1L, 100000001L, 101, 3, "10:00", "11:00", "A", "OPD");
            when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));
            when(appointmentRepository.findByPrepNurse(101)).thenReturn(List.of(appt));
            when(undergoesRepository.findByAssistingNurse(101)).thenReturn(List.of());
            when(onCallRepository.findByNurse(101)).thenReturn(List.of());
            when(dutyShiftRepository.findByPersonTypeAndPersonId("nurse", 101)).thenReturn(List.of());
            when(physicianRepository.findAll()).thenReturn(List.of(physician));
            when(patientRepository.findAll()).thenReturn(List.of(patient));
            when(roomRepository.findAll()).thenReturn(List.of());
            when(stayRepository.findAll()).thenReturn(List.of());

            NurseDashboardResponse result = dashboardService.nurseDashboard(101);

            assertThat(result.assignments()).hasSize(1);
            AssignmentView assignment = result.assignments().get(0);
            assertThat(assignment.type()).isEqualTo("Appointment");
            assertThat(assignment.patient()).isEqualTo(patient);
            assertThat(assignment.supervisingDoctor()).isEqualTo(physician);
            assertThat(assignment.room()).isEqualTo("A");
            assertThat(assignment.when()).isEqualTo("10:00 \u2192 11:00");
        }

        @Test
        @DisplayName("turns an operation-assist record into an Operation-assist-type assignment with a formatted room label")
        void operationAssistAssignmentWithRoom() {
            Undergoes op = new Undergoes("u1", 100000001L, 6, 3215, "2008-05-02", 3, 101);
            Stay stay = new Stay(3215, 100000001L, 111, "2008-05-01", "2008-05-04");
            Room room = new Room(111, "Single", 1, 1, 0);
            when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));
            when(appointmentRepository.findByPrepNurse(101)).thenReturn(List.of());
            when(undergoesRepository.findByAssistingNurse(101)).thenReturn(List.of(op));
            when(onCallRepository.findByNurse(101)).thenReturn(List.of());
            when(dutyShiftRepository.findByPersonTypeAndPersonId("nurse", 101)).thenReturn(List.of());
            when(physicianRepository.findAll()).thenReturn(List.of(physician));
            when(patientRepository.findAll()).thenReturn(List.of(patient));
            when(roomRepository.findAll()).thenReturn(List.of(room));
            when(stayRepository.findAll()).thenReturn(List.of(stay));

            NurseDashboardResponse result = dashboardService.nurseDashboard(101);

            AssignmentView assignment = result.assignments().get(0);
            assertThat(assignment.type()).isEqualTo("Operation assist");
            assertThat(assignment.room()).isEqualTo("111 (Floor 1, Block 1)");
            assertThat(assignment.when()).isEqualTo("2008-05-02");
        }

        @Test
        @DisplayName("labels the room 'Unassigned' when the operation's stay has no matching room")
        void operationAssistAssignmentWithoutRoom() {
            Undergoes op = new Undergoes("u1", 100000001L, 6, 404, "2008-05-02", 3, 101);
            when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));
            when(appointmentRepository.findByPrepNurse(101)).thenReturn(List.of());
            when(undergoesRepository.findByAssistingNurse(101)).thenReturn(List.of(op));
            when(onCallRepository.findByNurse(101)).thenReturn(List.of());
            when(dutyShiftRepository.findByPersonTypeAndPersonId("nurse", 101)).thenReturn(List.of());
            when(physicianRepository.findAll()).thenReturn(List.of(physician));
            when(patientRepository.findAll()).thenReturn(List.of(patient));
            when(roomRepository.findAll()).thenReturn(List.of());
            when(stayRepository.findAll()).thenReturn(List.of());

            NurseDashboardResponse result = dashboardService.nurseDashboard(101);

            assertThat(result.assignments().get(0).room()).isEqualTo("Unassigned");
        }

        @Test
        @DisplayName("combines appointment and operation-assist assignments into one list")
        void combinesBothAssignmentTypes() {
            Appointment appt = new Appointment(1L, 100000001L, 101, 3, "10:00", "11:00", "A", "OPD");
            Undergoes op = new Undergoes("u1", 100000001L, 6, 404, "2008-05-02", 3, 101);
            when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));
            when(appointmentRepository.findByPrepNurse(101)).thenReturn(List.of(appt));
            when(undergoesRepository.findByAssistingNurse(101)).thenReturn(List.of(op));
            when(onCallRepository.findByNurse(101)).thenReturn(List.of());
            when(dutyShiftRepository.findByPersonTypeAndPersonId("nurse", 101)).thenReturn(List.of());
            when(physicianRepository.findAll()).thenReturn(List.of(physician));
            when(patientRepository.findAll()).thenReturn(List.of(patient));
            when(roomRepository.findAll()).thenReturn(List.of());
            when(stayRepository.findAll()).thenReturn(List.of());

            NurseDashboardResponse result = dashboardService.nurseDashboard(101);

            assertThat(result.assignments()).hasSize(2);
        }
    }
}
