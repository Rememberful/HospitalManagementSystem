package com.hms.hospital.controller;

import com.hms.hospital.entity.*;
import com.hms.hospital.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(ReferenceDataController.class)
class ReferenceDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private PhysicianRepository physicianRepository;
    @MockBean private NurseRepository nurseRepository;
    @MockBean private PatientRepository patientRepository;
    @MockBean private DepartmentRepository departmentRepository;
    @MockBean private MedicalProcedureRepository procedureRepository;
    @MockBean private AppointmentRepository appointmentRepository;
    @MockBean private RoomRepository roomRepository;
    @MockBean private MedicationRepository medicationRepository;
    @MockBean private AffiliatedWithRepository affiliatedWithRepository;
    @MockBean private TrainedInRepository trainedInRepository;
    @MockBean private PrescriptionRepository prescriptionRepository;
    @MockBean private BlockRepository blockRepository;
    @MockBean private OnCallRepository onCallRepository;
    @MockBean private StayRepository stayRepository;
    @MockBean private UndergoesRepository undergoesRepository;
    @MockBean private DutyShiftRepository dutyShiftRepository;
    @MockBean private BillingRepository billingRepository;
    @MockBean private SalaryRepository salaryRepository;
    @MockBean private AppUserRepository appUserRepository;

    @Test
    void physiciansEndpointReturnsFullList() throws Exception {
        Physician physician = new Physician(1, "John Dorian", "Staff Internist", 111111111L);
        when(physicianRepository.findAll()).thenReturn(List.of(physician));

        mockMvc.perform(get("/physicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Name").value("John Dorian"));
    }

    @Test
    void nursesEndpointReturnsFullList() throws Exception {
        Nurse nurse = new Nurse(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);
        when(nurseRepository.findAll()).thenReturn(List.of(nurse));

        mockMvc.perform(get("/nurses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Name").value("Carla Espinosa"));
    }

    @Test
    void patientsEndpointReturnsFullList() throws Exception {
        Patient patient = new Patient(100000001L, "John Smith", "42 Foobar Lane", "555-0256", 68476213L, 1);
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].SSN").value(100000001L));
    }

    @Test
    void roomsEndpointReturnsFullList() throws Exception {
        Room room = new Room(101, "Single", 1, 1, 0);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].RoomNumber").value(101));
    }

    @Test
    void usersEndpointNeverExposesPasswordHashes() throws Exception {
        AppUser user = new AppUser(1, "admin", "$2a$10$somehashsomehashsomehash", "admin", "System Admin", null, null);
        when(appUserRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }
}
