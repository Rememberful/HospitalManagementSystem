package com.hms.hospital.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.hospital.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Runs the real Spring Security filter chain against the real access policy, with real signed
 * tokens and the real seeded accounts (see seed-data.json). Nothing about auth is mocked here —
 * these are the tests that answer "can the wrong person actually get in?".
 *
 * Seeded fixtures used throughout:
 *   admin / admin123          (admin)
 *   jdorian / doctor123       (doctor, EmployeeID 1)     ereid / doctor123 (doctor, EmployeeID 2)
 *   cespinosa / nurse123      (nurse,  EmployeeID 101)   lroberts / nurse123 (nurse, EmployeeID 102)
 *   jsmith / patient123       (patient, SSN 100000001)   gritchie / patient123 (patient, SSN 100000002)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppUserRepository appUserRepository;

    // ── helpers ───────────────────────────────────────────────────────────────

    private String tokenFor(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private String admin()   throws Exception { return tokenFor("admin", "admin123"); }
    private String doctor()  throws Exception { return tokenFor("jdorian", "doctor123"); }
    private String nurse()   throws Exception { return tokenFor("cespinosa", "nurse123"); }
    private String patient() throws Exception { return tokenFor("jsmith", "patient123"); }

    private MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }

    // ── authentication ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("authentication")
    class Authentication {

        @Test
        @DisplayName("a protected endpoint with no token is 401 with the standard JSON error")
        void noTokenIs401() throws Exception {
            mockMvc.perform(get("/physicians"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Authentication required"));
        }

        @Test
        @DisplayName("login succeeds for a seeded account, returning a token and no password")
        void loginSucceeds() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.user.role").value("admin"))
                    .andExpect(jsonPath("$.user.password").doesNotExist());
        }

        @Test
        @DisplayName("a wrong password is 401 and no token is issued")
        void wrongPassword() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.token").doesNotExist());
        }

        @Test
        @DisplayName("an unknown username gets the identical response as a wrong password")
        void unknownUserIndistinguishable() throws Exception {
            String wrongPassword = mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content("{\"username\":\"admin\",\"password\":\"nope-indistinguishable\"}"))
                    .andReturn().getResponse().getContentAsString();
            String unknownUser = mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content("{\"username\":\"nobody-here\",\"password\":\"nope\"}"))
                    .andReturn().getResponse().getContentAsString();

            assertThat(objectMapper.readTree(unknownUser).get("message"))
                    .isEqualTo(objectMapper.readTree(wrongPassword).get("message"));
        }

        @Test
        @DisplayName("stored passwords are BCrypt hashes, never the plaintext")
        void passwordsAreHashedAtRest() {
            var admin = appUserRepository.findByUsername("admin").orElseThrow();

            assertThat(admin.getPassword()).isNotEqualTo("admin123");
            assertThat(admin.getPassword()).startsWith("$2");
        }

        @Test
        @DisplayName("the plaintext password can't be used as a token or in place of one")
        void plaintextIsNotACredential() throws Exception {
            mockMvc.perform(get("/physicians").header("Authorization", "Bearer admin123"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("garbage in the Authorization header is 401, not a 500")
        void garbageToken() throws Exception {
            mockMvc.perform(get("/physicians").header("Authorization", "Bearer not.a.jwt"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("a token with a tampered signature is rejected")
        void tamperedToken() throws Exception {
            String token = admin();
            char last = token.charAt(token.length() - 1);
            String tampered = token.substring(0, token.length() - 1) + (last == 'A' ? 'B' : 'A');

            mockMvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + tampered))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("a non-Bearer scheme (e.g. Basic) doesn't authenticate")
        void basicAuthIgnored() throws Exception {
            mockMvc.perform(get("/physicians").header("Authorization", "Basic YWRtaW46YWRtaW4xMjM="))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("a valid token works on a general endpoint")
        void validTokenWorks() throws Exception {
            mockMvc.perform(as(patient(), get("/physicians")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CORS preflight succeeds without a token (browsers never send one on OPTIONS)")
        void corsPreflight() throws Exception {
            mockMvc.perform(options("/api/patient/100000001/dashboard")
                            .header("Origin", "http://localhost:5173")
                            .header("Access-Control-Request-Method", "GET")
                            .header("Access-Control-Request-Headers", "authorization"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        }

        @Test
        @DisplayName("CORS does not allow an arbitrary origin")
        void corsRejectsUnknownOrigin() throws Exception {
            mockMvc.perform(options("/physicians")
                            .header("Origin", "http://evil.example")
                            .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("repeated failed logins for the same username eventually lock it out, even with the right password")
        void repeatedFailuresLockOut() throws Exception {
            // A throwaway, never-succeeding username, so this test can't lock out an account
            // any other test in this class depends on.
            String username = "lockout-test-user";
            String badLogin = "{\"username\":\"" + username + "\",\"password\":\"wrong\"}";

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/login").contentType("application/json").content(badLogin))
                        .andExpect(status().isUnauthorized());
            }

            mockMvc.perform(post("/api/auth/login").contentType("application/json").content(badLogin))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(header().exists("Retry-After"));
        }
    }

    // ── patients: own data only ───────────────────────────────────────────────

    @Nested
    @DisplayName("a patient")
    class PatientAccess {

        @Test
        @DisplayName("can view their own dashboard")
        void ownDashboard() throws Exception {
            mockMvc.perform(as(patient(), get("/api/patient/100000001/dashboard")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profile.Name").value("John Smith"));
        }

        @Test
        @DisplayName("cannot view another patient's dashboard by changing the SSN in the URL")
        void otherPatientsDashboard() throws Exception {
            mockMvc.perform(as(patient(), get("/api/patient/100000002/dashboard")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("You don't have permission to do that"));
        }

        @Test
        @DisplayName("cannot read another patient's insurance ID")
        void otherPatientsInsurance() throws Exception {
            mockMvc.perform(as(patient(), get("/api/patient/insurance/100000002")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("can read their own insurance ID")
        void ownInsurance() throws Exception {
            mockMvc.perform(as(patient(), get("/api/patient/insurance/100000001")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("cannot edit another patient's address or phone")
        void cannotEditOthersContactDetails() throws Exception {
            mockMvc.perform(as(patient(), put("/api/patient/address/100000002")
                            .contentType("application/json").content("{\"Address\":\"hijacked\"}")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(as(patient(), put("/api/patient/phone/100000002")
                            .contentType("application/json").content("{\"Phone\":\"000\"}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("can edit their own address")
        void canEditOwnAddress() throws Exception {
            mockMvc.perform(as(patient(), put("/api/patient/address/100000001")
                            .contentType("application/json").content("{\"Address\":\"1 New Street\"}")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Address").value("1 New Street"));
        }

        @Test
        @DisplayName("cannot list all patients, on any of the URL spellings that reach the list")
        void cannotListAllPatients() throws Exception {
            String token = patient();
            mockMvc.perform(as(token, get("/patients"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/patient"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/patient/"))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot list all appointments, billing, or other operational rosters")
        void cannotListOperationalData() throws Exception {
            String token = patient();
            for (String path : new String[]{"/api/appointment", "/api/appointment/", "/appointments",
                    "/api/billing", "/api/billing/", "/billing", "/prescribes", "/stays", "/undergoes",
                    "/on_call", "/duty_shifts", "/salaries", "/nurses", "/users", "/api/nurse", "/api/nurse/",
                    "/api/prescribe", "/api/undergoes"}) {
                mockMvc.perform(as(token, get(path)))
                        .andExpect(status().isForbidden());
            }
        }

        @Test
        @DisplayName("can look up themselves by SSN, but not another patient")
        void singlePatientLookup() throws Exception {
            String token = patient();
            mockMvc.perform(as(token, get("/api/patient/100000001"))).andExpect(status().isOk());
            mockMvc.perform(as(token, get("/api/patient/100000002"))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot reach any admin endpoint")
        void cannotReachAdmin() throws Exception {
            String token = patient();
            mockMvc.perform(as(token, get("/api/admin/stats"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/admin/patients/100000002"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, delete("/api/admin/patients/100000002"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, put("/api/admin/patients/100000002")
                            .contentType("application/json").content("{\"Name\":\"x\"}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot create accounts, set salaries, or write master data")
        void cannotWriteAdminData() throws Exception {
            String token = patient();
            mockMvc.perform(as(token, post("/api/users").contentType("application/json")
                            .content("{\"id\":99,\"username\":\"x\",\"password\":\"p\",\"role\":\"admin\",\"displayName\":\"X\"}")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(as(token, post("/api/salary").contentType("application/json")
                            .content("{\"physician\":1,\"baseSalary\":1,\"commissionRate\":0.1}")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(as(token, post("/api/medication").contentType("application/json")
                            .content("{\"Code\":999,\"Name\":\"x\"}")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(as(token, post("/api/billing").contentType("application/json")
                            .content("{\"patient\":100000001,\"items\":[{\"type\":\"x\",\"label\":\"x\",\"amount\":1}]}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot mark an invoice as paid")
        void cannotMarkPaid() throws Exception {
            mockMvc.perform(as(patient(), put("/api/billing/anything/pay")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot prescribe medication or record procedures")
        void cannotDoClinicalWork() throws Exception {
            String token = patient();
            mockMvc.perform(as(token, post("/api/prescribe").contentType("application/json")
                            .content("{\"physician\":1,\"patient\":100000001,\"medication\":1,\"date\":\"2030-01-01\",\"dose\":\"x\"}")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(as(token, post("/api/undergoes").contentType("application/json")
                            .content("{\"patient\":100000001,\"procedures\":1,\"dateUndergoes\":\"2030-01-01\",\"physician\":1}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot read another patient's prescriptions or bills, but can read their own")
        void cannotReadOthersPrescriptionsOrBills() throws Exception {
            String token = patient();
            mockMvc.perform(as(token, get("/api/prescribe/patient/100000002"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/billing/patient/100000002"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/prescribe/patient/100000001"))).andExpect(status().isOk());
            mockMvc.perform(as(token, get("/api/billing/patient/100000001"))).andExpect(status().isOk());
        }

        @Test
        @DisplayName("cannot use a doctor's or nurse's dashboard endpoints")
        void cannotUseStaffDashboards() throws Exception {
            String token = patient();
            mockMvc.perform(as(token, get("/api/doctor/1/dashboard"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/nurse/101/dashboard"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/patient/physician/1"))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot book an appointment on behalf of another patient")
        void cannotBookForSomeoneElse() throws Exception {
            mockMvc.perform(as(patient(), post("/api/appointment/").contentType("application/json")
                            .content("{\"AppointmentID\":71001,\"Patient\":100000002,\"Physician\":1,"
                                    + "\"Starto\":\"2030-01-01 09:00\",\"Endo\":\"2030-01-01 09:30\",\"ExaminationRoom\":\"A\"}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("can book an appointment for themselves")
        void canBookForSelf() throws Exception {
            mockMvc.perform(as(patient(), post("/api/appointment/").contentType("application/json")
                            .content("{\"AppointmentID\":71002,\"Patient\":100000001,\"Physician\":1,"
                                    + "\"Starto\":\"2030-01-01 09:00\",\"Endo\":\"2030-01-01 09:30\",\"ExaminationRoom\":\"A\"}")))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("can view and edit their own appointment, but not someone else's")
        void appointmentOwnership() throws Exception {
            // admin books one appointment for each of two patients
            String adminToken = admin();
            for (String[] appt : new String[][]{{"71003", "100000001"}, {"71004", "100000002"}}) {
                mockMvc.perform(as(adminToken, post("/api/appointment/").contentType("application/json")
                                .content("{\"AppointmentID\":" + appt[0] + ",\"Patient\":" + appt[1] + ",\"Physician\":1,"
                                        + "\"Starto\":\"2030-01-01 09:00\",\"Endo\":\"2030-01-01 09:30\",\"ExaminationRoom\":\"A\"}")))
                        .andExpect(status().isCreated());
            }
            String token = patient();

            mockMvc.perform(as(token, get("/api/appointment/71003"))).andExpect(status().isOk());
            mockMvc.perform(as(token, get("/api/appointment/71004"))).andExpect(status().isForbidden());

            mockMvc.perform(as(token, put("/api/patient/appointment/71003")
                            .contentType("application/json").content("{\"ExaminationRoom\":\"B\"}")))
                    .andExpect(status().isOk());
            mockMvc.perform(as(token, put("/api/patient/appointment/71004")
                            .contentType("application/json").content("{\"ExaminationRoom\":\"B\"}")))
                    .andExpect(status().isForbidden());
        }
    }

    // ── doctors ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("a doctor")
    class DoctorAccess {

        @Test
        @DisplayName("can view their own dashboard")
        void ownDashboard() throws Exception {
            mockMvc.perform(as(doctor(), get("/api/doctor/1/dashboard")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profile.Name").value("John Dorian"));
        }

        @Test
        @DisplayName("cannot view another doctor's dashboard")
        void otherDoctorsDashboard() throws Exception {
            mockMvc.perform(as(doctor(), get("/api/doctor/2/dashboard")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("can list their own patients but not another doctor's")
        void ownPatientsOnly() throws Exception {
            String token = doctor();
            mockMvc.perform(as(token, get("/api/patient/physician/1"))).andExpect(status().isOk());
            mockMvc.perform(as(token, get("/api/patient/physician/2"))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot open a patient dashboard directly (that's the patient's own view, or admin's)")
        void cannotOpenPatientDashboard() throws Exception {
            mockMvc.perform(as(doctor(), get("/api/patient/100000001/dashboard")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("can prescribe medication")
        void canPrescribe() throws Exception {
            mockMvc.perform(as(doctor(), post("/api/prescribe").contentType("application/json")
                            .content("{\"physician\":1,\"patient\":100000001,\"medication\":1,\"date\":\"2030-01-01\",\"dose\":\"1 daily\"}")))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("cannot create accounts, set salaries, or touch the admin endpoints")
        void cannotDoAdminWork() throws Exception {
            String token = doctor();
            mockMvc.perform(as(token, post("/api/users").contentType("application/json")
                            .content("{\"id\":98,\"username\":\"x\",\"password\":\"p\",\"role\":\"admin\",\"displayName\":\"X\"}")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(as(token, post("/api/salary").contentType("application/json")
                            .content("{\"physician\":1,\"baseSalary\":999999,\"commissionRate\":0.9}")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/admin/stats"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, delete("/api/admin/physicians/2"))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot read the full patient roster or billing")
        void cannotListPatientsOrBilling() throws Exception {
            String token = doctor();
            mockMvc.perform(as(token, get("/patients"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/patient/"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/api/billing"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/salaries"))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot admit or discharge patients")
        void cannotAdmit() throws Exception {
            mockMvc.perform(as(doctor(), post("/api/stay/admit").contentType("application/json")
                            .content("{\"stayId\":9001,\"patient\":100000001,\"room\":101,\"stayStart\":\"2030-01-01\"}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("can look up a specific patient by SSN")
        void canLookUpPatient() throws Exception {
            mockMvc.perform(as(doctor(), get("/api/patient/100000001"))).andExpect(status().isOk());
        }
    }

    // ── nurses ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("a nurse")
    class NurseAccess {

        @Test
        @DisplayName("can view their own dashboard, but not another nurse's")
        void dashboardOwnership() throws Exception {
            String token = nurse();
            mockMvc.perform(as(token, get("/api/nurse/101/dashboard"))).andExpect(status().isOk());
            mockMvc.perform(as(token, get("/api/nurse/102/dashboard"))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot view a doctor's dashboard or prescribe")
        void cannotDoDoctorWork() throws Exception {
            String token = nurse();
            mockMvc.perform(as(token, get("/api/doctor/1/dashboard"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, post("/api/prescribe").contentType("application/json")
                            .content("{\"physician\":1,\"patient\":100000001,\"medication\":1,\"date\":\"2030-01-01\",\"dose\":\"x\"}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot reach admin endpoints or the full rosters")
        void cannotReachAdmin() throws Exception {
            String token = nurse();
            mockMvc.perform(as(token, get("/api/admin/stats"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/patients"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, get("/users"))).andExpect(status().isForbidden());
            mockMvc.perform(as(token, post("/api/users").contentType("application/json")
                            .content("{\"id\":97,\"username\":\"x\",\"password\":\"p\",\"role\":\"admin\",\"displayName\":\"X\"}")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("can reach the admissions endpoints (ward work)")
        void canReachAdmissions() throws Exception {
            mockMvc.perform(as(nurse(), get("/api/stay"))).andExpect(status().isOk());
        }

        @Test
        @DisplayName("can read a single room (used by the room detail page)")
        void canReadRoom() throws Exception {
            mockMvc.perform(as(nurse(), get("/api/room/101"))).andExpect(status().isOk());
        }
    }

    // ── admin ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("an admin")
    class AdminAccess {

        @Test
        @DisplayName("can reach stats and every roster")
        void canReachEverything() throws Exception {
            String token = admin();
            for (String path : new String[]{"/api/admin/stats", "/patients", "/nurses", "/users", "/salaries",
                    "/billing", "/appointments", "/api/appointment", "/api/patient/", "/api/billing"}) {
                mockMvc.perform(as(token, get(path))).andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("can open any patient, doctor and nurse dashboard")
        void canOpenAnyDashboard() throws Exception {
            String token = admin();
            mockMvc.perform(as(token, get("/api/patient/100000002/dashboard"))).andExpect(status().isOk());
            mockMvc.perform(as(token, get("/api/doctor/2/dashboard"))).andExpect(status().isOk());
            mockMvc.perform(as(token, get("/api/nurse/102/dashboard"))).andExpect(status().isOk());
        }

        @Test
        @DisplayName("the user list never contains password hashes")
        void userListHasNoPasswords() throws Exception {
            String body = mockMvc.perform(as(admin(), get("/api/users")))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(body).doesNotContain("password").doesNotContain("$2a$").doesNotContain("$2b$");
        }

        @Test
        @DisplayName("a created account is stored hashed and can immediately log in")
        void createdAccountIsHashedAndUsable() throws Exception {
            mockMvc.perform(as(admin(), post("/api/users").contentType("application/json")
                            .content("{\"id\":501,\"username\":\"newnurse\",\"password\":\"S3cret-pw!\","
                                    + "\"role\":\"nurse\",\"displayName\":\"New Nurse\",\"employeeId\":103}")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.password").doesNotExist());

            var stored = appUserRepository.findByUsername("newnurse").orElseThrow();
            assertThat(stored.getPassword()).isNotEqualTo("S3cret-pw!").startsWith("$2");

            mockMvc.perform(post("/api/auth/login").contentType("application/json")
                            .content("{\"username\":\"newnurse\",\"password\":\"S3cret-pw!\"}"))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/auth/login").contentType("application/json")
                            .content("{\"username\":\"newnurse\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("can create and delete master data through the admin endpoints")
        void canManageMasterData() throws Exception {
            String token = admin();
            mockMvc.perform(as(token, post("/api/physician/post").contentType("application/json")
                            .content("{\"EmployeeID\":9001,\"Name\":\"Sec Test\",\"Position\":\"Resident\",\"SSN\":900100900}")))
                    .andExpect(status().isCreated());
            mockMvc.perform(as(token, delete("/api/admin/physicians/9001"))).andExpect(status().isOk());
        }
    }
}
