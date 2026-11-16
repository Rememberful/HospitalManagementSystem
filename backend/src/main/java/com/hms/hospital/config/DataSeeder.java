package com.hms.hospital.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.hospital.entity.*;
import com.hms.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * One-time seed of the data that used to live in server/db.json, so the app has the same
 * sample hospital on first run as the old mock API did. Runs only when the target table is
 * empty, so it never clobbers data you've since added through the app.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ObjectMapper objectMapper;

    private final PhysicianRepository physicianRepository;
    private final DepartmentRepository departmentRepository;
    private final AffiliatedWithRepository affiliatedWithRepository;
    private final MedicalProcedureRepository procedureRepository;
    private final TrainedInRepository trainedInRepository;
    private final PatientRepository patientRepository;
    private final NurseRepository nurseRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final BlockRepository blockRepository;
    private final RoomRepository roomRepository;
    private final OnCallRepository onCallRepository;
    private final StayRepository stayRepository;
    private final UndergoesRepository undergoesRepository;
    private final AppUserRepository appUserRepository;
    private final DutyShiftRepository dutyShiftRepository;
    private final BillingRepository billingRepository;
    private final SalaryRepository salaryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        JsonNode root = objectMapper.readTree(new ClassPathResource("seed-data.json").getInputStream());

        // Order matters only in that it's easier to read top-down; there are no FK constraints
        // enforced at the DB level here, so any order is actually safe.
        seed(root, "physicians", Physician.class, physicianRepository);
        seed(root, "departments", Department.class, departmentRepository);
        seed(root, "affiliated_with", AffiliatedWith.class, affiliatedWithRepository);
        seed(root, "procedures", MedicalProcedure.class, procedureRepository);
        seed(root, "trained_in", TrainedIn.class, trainedInRepository);
        seed(root, "patients", Patient.class, patientRepository);
        seed(root, "nurses", Nurse.class, nurseRepository);
        seed(root, "appointments", Appointment.class, appointmentRepository);
        seed(root, "medications", Medication.class, medicationRepository);
        seed(root, "prescribes", Prescription.class, prescriptionRepository);
        seed(root, "blocks", Block.class, blockRepository);
        seed(root, "rooms", Room.class, roomRepository);
        seed(root, "on_call", OnCall.class, onCallRepository);
        seed(root, "stays", Stay.class, stayRepository);
        seed(root, "undergoes", Undergoes.class, undergoesRepository);
        seedUsers(root);
        seed(root, "duty_shifts", DutyShift.class, dutyShiftRepository);
        seed(root, "billing", Billing.class, billingRepository);
        seed(root, "salaries", Salary.class, salaryRepository);
    }

    /**
     * seed-data.json keeps its sample logins in plaintext for readability/diffability — this
     * is the one place that ever reads that field: every password is hashed here, once, before
     * it touches the database. AuthServiceImpl only ever compares against the hash afterward.
     */
    private void seedUsers(JsonNode root) {
        if (appUserRepository.count() > 0) return;
        JsonNode node = root.get("users");
        if (node == null || !node.isArray()) return;
        try {
            List<AppUser> users = objectMapper.readerForListOf(AppUser.class).readValue(node);
            users.forEach(u -> u.setPassword(passwordEncoder.encode(u.getPassword())));
            appUserRepository.saveAll(users);
            log.info("Seeded {} user account(s)", users.size());
        } catch (Exception e) {
            log.warn("Could not seed \'users\': {}", e.getMessage());
        }
    }

    private <T, ID> void seed(JsonNode root, String key, Class<T> type,
                               org.springframework.data.jpa.repository.JpaRepository<T, ID> repository) {
        if (repository.count() > 0) return; // already seeded / already has real data
        JsonNode node = root.get(key);
        if (node == null || !node.isArray()) return;
        try {
            List<T> records = objectMapper.readerForListOf(type).readValue(node);
            repository.saveAll(records);
            log.info("Seeded {} {} record(s)", records.size(), key);
        } catch (Exception e) {
            log.warn("Could not seed '{}': {}", key, e.getMessage());
        }
    }
}
