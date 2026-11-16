package com.hms.hospital.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.hospital.dto.response.UserResponse;
import com.hms.hospital.entity.AppUser;
import com.hms.hospital.entity.Appointment;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.entity.Medication;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.entity.Patient;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.Room;
import com.hms.hospital.entity.Salary;
import com.hms.hospital.entity.Stay;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.AppUserRepository;
import com.hms.hospital.repository.AppointmentRepository;
import com.hms.hospital.repository.DepartmentRepository;
import com.hms.hospital.repository.MedicalProcedureRepository;
import com.hms.hospital.repository.MedicationRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.RoomRepository;
import com.hms.hospital.repository.SalaryRepository;
import com.hms.hospital.repository.StayRepository;
import com.hms.hospital.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mirrors the original mock API's generic /api/admin/:resource/:id routes. A PUT here is a
 * partial merge onto the existing record (field-by-field), same semantics as the lowdb
 * `.assign()` call it replaces — not a full replace.
 *
 * Only resources with a simple numeric key live here — the AdminController path is typed
 * as Long. Resources with a composite String key (blocks, on_call, undergoes, duty_shifts,
 * prescribes, affiliated_with, trained_in, billing) have their own dedicated controllers instead.
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final MedicalProcedureRepository procedureRepository;
    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final MedicationRepository medicationRepository;
    private final SalaryRepository salaryRepository;
    private final StayRepository stayRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> VALID_ROLES = Set.of("admin", "doctor", "nurse", "patient");

    /**
     * JSON name of each resource's primary key. A patch may never change it: saving a record
     * under a different key doesn't rename it, it silently creates a second row (and can be used
     * to overwrite an unrelated existing record).
     */
    private static final Map<String, String> ID_FIELD = Map.ofEntries(
            Map.entry("physicians", "EmployeeID"),
            Map.entry("nurses", "EmployeeID"),
            Map.entry("patients", "SSN"),
            Map.entry("departments", "DepartmentID"),
            Map.entry("procedures", "Code"),
            Map.entry("appointments", "AppointmentID"),
            Map.entry("rooms", "RoomNumber"),
            Map.entry("medications", "Code"),
            Map.entry("salaries", "physician"),
            Map.entry("stays", "StayID"),
            Map.entry("users", "id"));

    @Override
    public Object get(String resource, Long id) {
        return present(load(resource, id));
    }

    /** Never let a password hash leave the service, even to an admin. */
    private Object present(Object entity) {
        return entity instanceof AppUser user ? UserResponse.from(user) : entity;
    }

    private Object load(String resource, Long id) {
        return switch (resource) {
            case "physicians" -> physicianRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "nurses" -> nurseRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "patients" -> patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "departments" -> departmentRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "procedures" -> procedureRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "appointments" -> appointmentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "rooms" -> roomRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "medications" -> medicationRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "salaries" -> salaryRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "stays" -> stayRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            case "users" -> appUserRepository.findById(id.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException(resource));
            default -> throw new BadRequestException("Unknown or non-editable resource: " + resource);
        };
    }

    @Override
    public Object update(String resource, Long id, Map<String, Object> rawPatch) {
        Object current = load(resource, id);
        Map<String, Object> patch = sanitizePatch(resource, current, rawPatch);
        Object merged = mergePatch(current, patch);
        return present(switch (resource) {
            case "physicians" -> physicianRepository.save((Physician) merged);
            case "nurses" -> nurseRepository.save((Nurse) merged);
            case "patients" -> patientRepository.save((Patient) merged);
            case "departments" -> departmentRepository.save((Department) merged);
            case "procedures" -> procedureRepository.save((MedicalProcedure) merged);
            case "appointments" -> appointmentRepository.save((Appointment) merged);
            case "rooms" -> roomRepository.save((Room) merged);
            case "medications" -> medicationRepository.save((Medication) merged);
            case "salaries" -> salaryRepository.save((Salary) merged);
            case "stays" -> stayRepository.save((Stay) merged);
            case "users" -> appUserRepository.save((AppUser) merged);
            default -> throw new BadRequestException("Unknown or non-editable resource: " + resource);
        });
    }

    /**
     * Applies the rules that make a generic "merge these fields in" endpoint safe: the primary
     * key is immutable, and for user accounts a supplied password is hashed (a blank one is
     * treated as "leave unchanged") and a supplied role must be a real one.
     */
    private Map<String, Object> sanitizePatch(String resource, Object current, Map<String, Object> rawPatch) {
        Map<String, Object> patch = new HashMap<>(rawPatch);
        String idField = ID_FIELD.get(resource);
        if (idField != null) patch.remove(idField);

        if (current instanceof AppUser) {
            Object password = patch.remove("password");
            if (password instanceof String plain && !plain.isBlank()) {
                if (plain.length() < 8 || plain.length() > 72) {
                    throw new BadRequestException("password must be 8-72 characters");
                }
                patch.put("password", passwordEncoder.encode(plain));
            }
            Object role = patch.get("role");
            if (role != null) {
                String normalized = String.valueOf(role).toLowerCase();
                if (!VALID_ROLES.contains(normalized)) {
                    throw new BadRequestException("role must be one of admin, doctor, nurse, patient");
                }
                patch.put("role", normalized);
            }
        }
        return patch;
    }

    @Override
    public void delete(String resource, Long id) {
        Object existing = load(resource, id); // throws 404 if missing, matching original behaviour
        switch (resource) {
            case "physicians" -> physicianRepository.delete((Physician) existing);
            case "nurses" -> nurseRepository.delete((Nurse) existing);
            case "patients" -> patientRepository.delete((Patient) existing);
            case "departments" -> departmentRepository.delete((Department) existing);
            case "procedures" -> procedureRepository.delete((MedicalProcedure) existing);
            case "appointments" -> appointmentRepository.delete((Appointment) existing);
            case "rooms" -> roomRepository.delete((Room) existing);
            case "medications" -> medicationRepository.delete((Medication) existing);
            case "salaries" -> salaryRepository.delete((Salary) existing);
            case "stays" -> stayRepository.delete((Stay) existing);
            case "users" -> appUserRepository.delete((AppUser) existing);
            default -> throw new BadRequestException("Unknown or non-editable resource: " + resource);
        }
    }

    @SuppressWarnings("unchecked")
    private Object mergePatch(Object current, Map<String, Object> patch) {
        Map<String, Object> asMap = objectMapper.convertValue(current, Map.class);
        asMap.putAll(patch);
        return objectMapper.convertValue(asMap, current.getClass());
    }
}
