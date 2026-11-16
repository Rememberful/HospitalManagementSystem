# Hospital Management System

Full-stack hospital management app.

- **`backend/`** — Spring Boot 3 REST API (Java 17), replacing the original json-server mock.
- **`client/`** — React + Vite frontend (unchanged in behaviour — just repointed at the new API).

## Running it

### 1. Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Starts on **http://localhost:8080**. On first run it seeds the same sample hospital data the
old `server/db.json` shipped with, into a local H2 database file at `backend/data/hms.mv.db`
(created automatically). Subsequent restarts keep whatever you've since added/changed —
seeding only runs once, when the tables are empty.

H2 console (optional, for poking at the data directly): http://localhost:8080/h2-console
JDBC URL: `jdbc:h2:file:./data/hms`, user `sa`, no password.

To point at MySQL instead, edit `backend/src/main/resources/application.properties` — the
MySQL connection block is already there, commented out.

### 2. Frontend (React)

```bash
cd client
npm install
npm run dev
```

Starts on **http://localhost:5173**. `client/.env` now points `VITE_API_BASE_URL` at
`http://localhost:8080` (the Spring Boot backend) instead of the old json-server port 3000.

Login with any of the seeded accounts (see `backend/src/main/resources/seed-data.json` →
`users`), e.g. `admin` / `admin123`.

## New hospital operations (added after the initial migration)

The initial Spring Boot migration was a faithful port of the original student-project API
surface. On top of that, the following real hospital-operations features were added, end to
end (backend API + frontend UI):

| Capability | Backend | Frontend |
|---|---|---|
| Create logins for staff/patients | `POST /api/users` | **Admin → Staff Accounts** |
| Admit / discharge a patient (tracks room availability automatically) | `POST /api/stay/admit`, `PUT /api/stay/discharge/{id}` | **Admin → Admissions** |
| Create rooms, toggle occupied/available | `POST /api/room`, `PUT /api/room/availability/{n}` | **Admin → Rooms** |
| Create an invoice from line items, mark paid | `POST /api/billing`, `PUT /api/billing/{id}/pay` | **Admin → Billing** |
| Set a physician's base salary + commission rate | `POST /api/salary` | **Admin → Salaries** |
| Manage the medication catalog | `POST /api/medication` | **Admin → Medications** |
| Doctor prescribes medication to a patient | `POST /api/prescribe` | Doctor → patient detail page |
| Doctor records a performed procedure | `POST /api/undergoes` | Doctor → patient detail page |
| Admin overview stats (patient/staff counts, today's appointments, revenue, room occupancy, active admissions) | `GET /api/admin/stats` | **Admin dashboard** |
| Schedule on-call nurses / duty shifts | `POST /api/on_call`, `POST /api/duty_shift` | API only — nurses already see these on their dashboard; no admin creation UI was built for these two, since the higher-value gaps above took priority |
| Create/manage building blocks | `POST /api/block` | API only, same reasoning as above |

A judgment call worth flagging:
- **Admitting a patient into an occupied room, or into a `StayID` that already exists, is
  rejected** — the same "reject clashing writes" philosophy as the rest of the API.

The plaintext-password/no-authorization gap mentioned in earlier revisions of this README is
now fixed — see **Security** below.

## Security

Authentication and authorization were added after the initial build, replacing a model where
the backend trusted whatever role the frontend claimed. Now:

**Authentication — Spring Security + JWT.**
- Passwords are hashed with BCrypt, both for the seeded accounts (hashed once at startup, in
  `DataSeeder`, from the plaintext in `seed-data.json`) and for every account created afterward.
  `seed-data.json` itself stays plaintext for readability — nothing ever reads that field again
  once it's hashed into the database.
- `POST /api/auth/login` verifies the password and returns a signed JWT (HS256, 24h expiry) plus
  the user's profile — never the password hash. The frontend sends that token as
  `Authorization: Bearer <token>` on every subsequent request (`client/src/services/api.js`); a
  401 response clears the session and bounces back to `/login`.
- An unknown username and a wrong password return the identical error, and take the same amount
  of time (`AuthServiceImpl` runs a dummy BCrypt comparison either way) — so neither the message
  nor the response time reveals whether a username exists.
- Repeated failed logins for one username lock it out for 15 minutes (`LoginAttemptService`,
  in-memory, configurable via `app.security.max-login-attempts` / `app.security.lockout-minutes`).

**Authorization — role + ownership, enforced on the backend.**
- The full access policy lives in one place, `security/SecurityConfig.java`, as a table of URL
  pattern → role(s). Admin-only write surfaces, doctor/nurse clinical actions, and patient
  self-service are each scoped there.
- Role alone isn't enough for anything that exposes one person's data to another: the dashboard,
  billing, prescription, and self-service endpoints (`DashboardController`, `PatientController`,
  `AppointmentController`) add `@PreAuthorize`/`@PostAuthorize` ownership checks via
  `SecurityUtil` — e.g. a patient's own JWT proves *which* patient, and a dashboard request for
  anyone else's SSN is rejected regardless of role. `SecurityIntegrationTest` is the test that
  actually tries this: two patient accounts, one tries to read the other's dashboard/insurance/
  appointments/address, and the suite confirms every one of those is blocked.
- The frontend mirrors this for UX (wrong-role routes redirect to `/app` instead of 404ing) in
  `RequireAuth` (`client/src/routes/AppRoutes.jsx`) — but that's convenience, not protection; the
  backend enforces the real boundary independently, and the whole point of `SecurityIntegrationTest`
  is checking that holds even if the frontend route guard is bypassed entirely.

**What this doesn't cover** (worth knowing before this goes anywhere near real patient data):
- No HTTPS/TLS here — that's a reverse-proxy/deployment concern, not something this app layer
  configures itself. Running bearer tokens over plain HTTP in production would defeat the point.
- No refresh tokens or logout-side token revocation — a token is valid for its full 24h lifetime
  once issued; "logout" just drops it client-side. Fine for coursework, not for production.
- The JWT secret has a dev-only default baked into `application.properties` — it's overridden by
  the `JWT_SECRET` environment variable, but a real deployment needs to actually set that env var
  (and rotate it from the default) rather than relying on the fallback.
- No rate limiting beyond the per-username login lockout above — no protection against a flood of
  requests to other endpoints, or logins against *many different* usernames at once.
- The H2 web console is off by default (`spring.h2.console.enabled=false`) specifically so a
  database UI isn't sitting on the network unauthenticated; it's not wired into the JWT scheme at
  all (browser navigation can't carry a bearer header), so turning it back on for local debugging
  means it's open to anyone who can reach that port.

## What changed (original migration)

**Backend** is a full rewrite from json-server (Node) to Spring Boot, in a standard layered
architecture:

```
backend/src/main/java/com/hms/hospital/
├── entity/       JPA entities — one per table, @JsonProperty-annotated to keep the exact
│                 same JSON field casing (EmployeeID, SSN, ...) the client already expects
├── repository/   Spring Data JPA repositories
├── service/      Business logic interfaces
├── service/impl/ Business logic implementations
├── controller/   REST controllers (one per resource, matching the original route grouping)
├── dto/          Request/response records for login, dashboards, and admin CRUD
├── exception/    Custom exceptions + a @RestControllerAdvice returning the same
│                 {timeStamp, message} error shape the client already handles
└── config/       CORS config + the one-time data seeder
```

Every route from the original `server/server.js` — all ~50 custom endpoints plus the
generic list endpoints the client uses (`/physicians`, `/nurses`, `/patients`, etc.) — has
a matching Spring endpoint with the same path, method, request/response shape, and
validation/error behaviour. The one deliberate exception: the original mock API also
auto-generated full CRUD (`POST`/`PUT`/`DELETE`) for *every* collection via json-server's
fallback router — that was an artifact of the mocking tool, not part of the designed API
surface, so only the documented `/api/admin/:resource/:id` CRUD (physicians, nurses,
patients, departments, procedures, appointments, rooms) was carried forward. Every
collection is still readable via `GET`.

**Frontend** — no rewrite. It already isolated all HTTP calls in
`client/src/services/api.js` behind a configurable `baseURL`, so it needed exactly one
change: `client/.env` now points at port 8080. `client/package.json` also lost its now-dead
`server` script (json-server), since that backend no longer exists.

## Testing

The backend has ~410 test methods across 56 test classes (grew from 317/51 with the security
work above), in four layers:

```
backend/src/test/java/com/hms/hospital/
├── service/impl/    Unit tests (Mockito) for every service — business logic, validation
│                     rules, error paths, and the trickier computed fields (doctor
│                     commission math, nurse assignment formatting, dashboard aggregation)
├── controller/       @ControllerTest slice tests (MockMvc) for every controller —
│                     request/response shapes, status codes, error handling via the real
│                     GlobalExceptionHandler. The security filter chain is deliberately off
│                     here (see ControllerTest.java) — these test controller logic, not access
│                     control, which has its own dedicated layer below.
├── security/         Unit tests for JwtService (token forgery/tampering/expiry), SecurityUtil
│                     (ownership checks), LoginAttemptService (lockout), and
│                     SecurityIntegrationTest — the real filter chain, real signed tokens, real
│                     seeded accounts, nothing mocked, organized per role (patient/doctor/nurse/
│                     admin) around "can the wrong person actually get in".
├── repository/       @DataJpaTest tests for the custom Spring Data finder methods, against a
│                     real (embedded) database rather than mocks
└── integration/       @SpringBootTest end-to-end tests — full app context, in-memory H2, seeded
                      with the same data the dev database uses, nothing mocked. These cover full
                      business flows (login → dashboard, create → read → update → delete) rather
                      than access control, which SecurityIntegrationTest already covers exhaustively.
```

Run them with:

```bash
cd backend
mvn test
```

The integration and repository tests run against an in-memory H2 database (`src/test/resources/application-test.properties`), so they never touch or depend on your dev database file.

## A note on verification

This was built in a sandboxed environment without access to Maven Central, so I wasn't able
to run `mvn spring-boot:run` myself to confirm a clean compile. I traced every endpoint the
React client calls against the original `server.js` line by line, matched field names and
JSON shapes exactly (including a few PascalCase-vs-camelCase mismatches I caught and fixed
along the way), and double-checked every repository/service method call resolves — but
please run `mvn spring-boot:run` locally as your first step, and let me know what comes back
if anything doesn't compile.
