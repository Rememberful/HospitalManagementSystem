package com.hms.hospital.controller;

import com.hms.hospital.dto.response.UserResponse;
import com.hms.hospital.entity.*;
import com.hms.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The original mock API (json-server) auto-generated a plain GET /{resource} list route for
 * every top-level collection in db.json, on top of the deliberate /api/... routes. The React
 * client's reference-data lookups (e.g. populating dropdowns) rely on a few of these. This
 * controller reproduces that fallback for every collection so nothing that worked before is lost.
 */
@RestController
@RequiredArgsConstructor
public class ReferenceDataController {

    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final MedicalProcedureRepository procedureRepository;
    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final MedicationRepository medicationRepository;
    private final AffiliatedWithRepository affiliatedWithRepository;
    private final TrainedInRepository trainedInRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final BlockRepository blockRepository;
    private final OnCallRepository onCallRepository;
    private final StayRepository stayRepository;
    private final UndergoesRepository undergoesRepository;
    private final DutyShiftRepository dutyShiftRepository;
    private final BillingRepository billingRepository;
    private final SalaryRepository salaryRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/physicians")
    public List<Physician> physicians() { return physicianRepository.findAll(); }

    @GetMapping("/nurses")
    public List<Nurse> nurses() { return nurseRepository.findAll(); }

    @GetMapping("/patients")
    public List<Patient> patients() { return patientRepository.findAll(); }

    @GetMapping("/departments")
    public List<Department> departments() { return departmentRepository.findAll(); }

    @GetMapping("/procedures")
    public List<MedicalProcedure> procedures() { return procedureRepository.findAll(); }

    @GetMapping("/appointments")
    public List<Appointment> appointments() { return appointmentRepository.findAll(); }

    @GetMapping("/rooms")
    public List<Room> rooms() { return roomRepository.findAll(); }

    @GetMapping("/medications")
    public List<Medication> medications() { return medicationRepository.findAll(); }

    @GetMapping("/affiliated_with")
    public List<AffiliatedWith> affiliatedWith() { return affiliatedWithRepository.findAll(); }

    @GetMapping("/trained_in")
    public List<TrainedIn> trainedIn() { return trainedInRepository.findAll(); }

    @GetMapping("/prescribes")
    public List<Prescription> prescribes() { return prescriptionRepository.findAll(); }

    @GetMapping("/blocks")
    public List<Block> blocks() { return blockRepository.findAll(); }

    @GetMapping("/on_call")
    public List<OnCall> onCall() { return onCallRepository.findAll(); }

    @GetMapping("/stays")
    public List<Stay> stays() { return stayRepository.findAll(); }

    @GetMapping("/undergoes")
    public List<Undergoes> undergoes() { return undergoesRepository.findAll(); }

    @GetMapping("/duty_shifts")
    public List<DutyShift> dutyShifts() { return dutyShiftRepository.findAll(); }

    @GetMapping("/billing")
    public List<Billing> billing() { return billingRepository.findAll(); }

    @GetMapping("/salaries")
    public List<Salary> salaries() { return salaryRepository.findAll(); }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return appUserRepository.findAll().stream().map(UserResponse::from).toList();
    }
}
