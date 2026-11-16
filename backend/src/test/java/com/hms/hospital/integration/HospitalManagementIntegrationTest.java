package com.hms.hospital.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Exercises the real Spring context end to end — actual @Service beans, actual JPA
 * repositories, an in-memory H2 database seeded from the same seed-data.json the dev
 * database uses — with nothing mocked. These are the tests that most directly answer
 * "does this actually work end to end", as opposed to the unit/slice tests elsewhere.
 *
 * (Access control itself is covered exhaustively in SecurityIntegrationTest; here every call
 * just authenticates as the seeded admin so these tests can stay focused on business flows.)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HospitalManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    /** Logs in as the seeded admin through the real login endpoint and returns the bearer header value. */
    private String adminBearer() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void loginWithSeededAdminCredentialsSucceeds() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("admin"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void doctorDashboardForSeededPhysicianReturnsRealData() throws Exception {
        // EmployeeID 1 = John Dorian, seeded with real appointments/patients in seed-data.json
        mockMvc.perform(get("/api/doctor/1/dashboard").header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.Name").value("John Dorian"));
    }

    @Test
    void patientDashboardForSeededPatientResolvesRealPcp() throws Exception {
        // SSN 100000001 = John Smith, PCP = EmployeeID 1 = John Dorian
        mockMvc.perform(get("/api/patient/100000001/dashboard").header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.Name").value("John Smith"))
                .andExpect(jsonPath("$.primaryCarePhysician.Name").value("John Dorian"));
    }

    @Test
    void nurseDashboardForSeededNurseReturns200() throws Exception {
        mockMvc.perform(get("/api/nurse/101/dashboard").header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.Name").value("Carla Espinosa"));
    }

    @Test
    void fullPhysicianLifecycleThroughCreateAdminUpdateAndDelete() throws Exception {
        String auth = adminBearer();

        // Create
        mockMvc.perform(post("/api/physician/post")
                        .header("Authorization", auth)
                        .contentType("application/json")
                        .content("{\"EmployeeID\":500,\"Name\":\"Test Physician\",\"Position\":\"Resident\",\"SSN\":500500500}"))
                .andExpect(status().isCreated());

        // Read back through the admin generic endpoint
        mockMvc.perform(get("/api/admin/physicians/500").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("Test Physician"));

        // Partial update through the admin generic endpoint
        mockMvc.perform(put("/api/admin/physicians/500")
                        .header("Authorization", auth)
                        .contentType("application/json")
                        .content("{\"Position\":\"Attending Physician\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Position").value("Attending Physician"))
                .andExpect(jsonPath("$.Name").value("Test Physician")); // untouched field survives

        // Delete
        mockMvc.perform(delete("/api/admin/physicians/500").header("Authorization", auth))
                .andExpect(status().isOk());

        // Now gone
        mockMvc.perform(get("/api/admin/physicians/500").header("Authorization", auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void bookingAnAppointmentWithoutAPrepNurseAutoAssignsOneFromTheRoster() throws Exception {
        String auth = adminBearer();

        mockMvc.perform(post("/api/appointment/")
                        .header("Authorization", auth)
                        .contentType("application/json")
                        .content("{\"AppointmentID\":99999,\"Patient\":100000001,\"Physician\":1,"
                                + "\"Starto\":\"2030-01-01 09:00\",\"Endo\":\"2030-01-01 09:30\",\"ExaminationRoom\":\"A\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/appointments/99999").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PrepNurse").isNumber());
    }

    @Test
    void bookingADuplicateAppointmentIdReturns400WithMessage() throws Exception {
        String auth = adminBearer();
        String body = "{\"AppointmentID\":88888,\"Patient\":100000001,\"Physician\":1,"
                + "\"Starto\":\"2030-01-01 09:00\",\"Endo\":\"2030-01-01 09:30\",\"ExaminationRoom\":\"A\"}";

        mockMvc.perform(post("/api/appointment/").header("Authorization", auth).contentType("application/json").content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/appointment/").header("Authorization", auth).contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Appointment with this AppointmentID already exists"));
    }

    @Test
    void referenceDataListEndpointsAreReachableAndNonEmpty() throws Exception {
        mockMvc.perform(get("/physicians").header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
    }
}
