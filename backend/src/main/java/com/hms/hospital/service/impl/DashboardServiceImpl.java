package com.hms.hospital.service.impl;

import com.hms.hospital.dto.response.AssignmentView;
import com.hms.hospital.dto.response.DoctorDashboardResponse;
import com.hms.hospital.dto.response.NurseDashboardResponse;
import com.hms.hospital.dto.response.OperationView;
import com.hms.hospital.dto.response.PatientDashboardResponse;
import com.hms.hospital.dto.response.SalaryView;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.Room;
import com.hms.hospital.entity.Salary;
import com.hms.hospital.entity.Stay;
import com.hms.hospital.entity.Undergoes;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.AppointmentRepository;
import com.hms.hospital.repository.BillingRepository;
import com.hms.hospital.repository.DutyShiftRepository;
import com.hms.hospital.repository.MedicalProcedureRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.OnCallRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.PrescriptionRepository;
import com.hms.hospital.repository.RoomRepository;
import com.hms.hospital.repository.SalaryRepository;
import com.hms.hospital.repository.StayRepository;
import com.hms.hospital.repository.UndergoesRepository;
import com.hms.hospital.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;
    private final AppointmentRepository appointmentRepository;
    private final StayRepository stayRepository;
    private final BillingRepository billingRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UndergoesRepository undergoesRepository;
    private final DutyShiftRepository dutyShiftRepository;
    private final SalaryRepository salaryRepository;
    private final MedicalProcedureRepository procedureRepository;
    private final OnCallRepository onCallRepository;
    private final RoomRepository roomRepository;

    @Override
    public PatientDashboardResponse patientDashboard(Long ssn) {
        Patient profile = patientRepository.findById(ssn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient"));
        Physician pcp = profile.getPcp() == null ? null
                : physicianRepository.findById(profile.getPcp()).orElse(null);

        return new PatientDashboardResponse(
                profile,
                pcp,
                appointmentRepository.findByPatient(ssn),
                stayRepository.findByPatient(ssn),
                billingRepository.findByPatient(ssn),
                prescriptionRepository.findByPatient(ssn));
    }

    @Override
    public DoctorDashboardResponse doctorDashboard(Integer employeeId) {
        Physician profile = physicianRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Physician"));

        List<Appointment> appointments = appointmentRepository.findByPhysician(employeeId);
        List<Undergoes> operations = undergoesRepository.findByPhysician(employeeId);
        List<com.hms.hospital.entity.DutyShift> dutyShifts =
                dutyShiftRepository.findByPersonTypeAndPersonId("physician", employeeId);
        Salary salaryRecord = salaryRepository.findByPhysician(employeeId).orElse(null);

        Map<Integer, MedicalProcedure> proceduresByCode = procedureRepository.findAll().stream()
                .collect(Collectors.toMap(MedicalProcedure::getCode, Function.identity()));

        List<OperationView> operationsWithCost = operations.stream()
                .map(op -> {
                    MedicalProcedure proc = proceduresByCode.get(op.getProcedures());
                    double cost = proc != null && proc.getCost() != null ? proc.getCost() : 0;
                    String name = proc != null ? proc.getName() : null;
                    return OperationView.from(op, name, cost);
                })
                .toList();

        double totalProcedureRevenue = operationsWithCost.stream()
                .mapToDouble(OperationView::cost)
                .sum();

        SalaryView salary = null;
        if (salaryRecord != null) {
            double commissionEarned = Math.round(totalProcedureRevenue * salaryRecord.getCommissionRate() * 100.0) / 100.0;
            salary = new SalaryView(
                    salaryRecord.getPhysician(), salaryRecord.getBaseSalary(), salaryRecord.getCommissionRate(),
                    commissionEarned, salaryRecord.getBaseSalary() + commissionEarned);
        }

        Set<Long> patientIds = appointments.stream().map(Appointment::getPatient).collect(Collectors.toSet());
        List<Patient> patients = patientRepository.findBySsnIn(List.copyOf(patientIds));

        long generalCheckups = appointments.stream()
                .filter(a -> !"Admission".equals(a.getVisitType()))
                .count();

        return new DoctorDashboardResponse(
                profile, patients, appointments, operationsWithCost, generalCheckups, dutyShifts, salary);
    }

    @Override
    public NurseDashboardResponse nurseDashboard(Integer employeeId) {
        Nurse profile = nurseRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nurse"));

        List<Appointment> prepAppointments = appointmentRepository.findByPrepNurse(employeeId);
        List<Undergoes> assisting = undergoesRepository.findByAssistingNurse(employeeId);
        List<com.hms.hospital.entity.OnCall> onCall = onCallRepository.findByNurse(employeeId);
        List<com.hms.hospital.entity.DutyShift> dutyShifts =
                dutyShiftRepository.findByPersonTypeAndPersonId("nurse", employeeId);

        Map<Integer, Physician> physiciansById = physicianRepository.findAll().stream()
                .collect(Collectors.toMap(Physician::getEmployeeId, Function.identity()));
        Map<Long, Patient> patientsBySsn = patientRepository.findAll().stream()
                .collect(Collectors.toMap(Patient::getSsn, Function.identity()));
        Map<Integer, Room> roomsByNumber = roomRepository.findAll().stream()
                .collect(Collectors.toMap(Room::getRoomNumber, Function.identity()));
        Map<Integer, Stay> staysById = stayRepository.findAll().stream()
                .collect(Collectors.toMap(Stay::getStayId, Function.identity()));

        List<AssignmentView> assignments = prepAppointments.stream()
                .map(a -> new AssignmentView(
                        "Appointment",
                        patientsBySsn.get(a.getPatient()),
                        physiciansById.get(a.getPhysician()),
                        a.getExaminationRoom(),
                        a.getStarto() + " \u2192 " + a.getEndo()))
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        List<AssignmentView> assistingAssignments = assisting.stream()
                .map(u -> {
                    Stay stay = staysById.get(u.getStay());
                    Room room = stay != null ? roomsByNumber.get(stay.getRoom()) : null;
                    String roomLabel = room != null
                            ? room.getRoomNumber() + " (Floor " + room.getBlockFloor() + ", Block " + room.getBlockCode() + ")"
                            : "Unassigned";
                    return new AssignmentView(
                            "Operation assist",
                            patientsBySsn.get(u.getPatient()),
                            physiciansById.get(u.getPhysician()),
                            roomLabel,
                            u.getDateUndergoes());
                })
                .toList();

        assignments.addAll(assistingAssignments);

        return new NurseDashboardResponse(profile, assignments, onCall, dutyShifts);
    }
}
