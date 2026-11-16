import axios from 'axios';

const SESSION_KEY = 'hms-user';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000',
  headers: { 'Content-Type': 'application/json' },
});

// Every request carries whatever JWT is currently stored — this is what actually makes
// the backend's role/ownership checks see "who's asking" instead of a fully anonymous call.
api.interceptors.request.use((config) => {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    const token = raw ? JSON.parse(raw).token : null;
    if (token) config.headers.Authorization = `Bearer ${token}`;
  } catch {
    // corrupt/unavailable storage — proceed unauthenticated, the backend will 401 as normal
  }
  return config;
});

api.interceptors.response.use(
  r => r,
  err => {
    // An expired/invalid token or a deleted account looks identical from here: the session
    // is no longer good, so drop it and send the person back to sign in. We avoid doing this
    // for the login call itself (a bad password shouldn't wipe a session that doesn't exist yet).
    const isLoginCall = err.config?.url?.includes('/api/auth/login');
    if (err.response?.status === 401 && !isLoginCall) {
      sessionStorage.removeItem(SESSION_KEY);
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    const e = new Error(err.response?.data?.message || err.message || 'API error');
    e.status = err.response?.status || (err.request ? 503 : 500);
    return Promise.reject(e);
  }
);

export default api;

// ── Auth ──────────────────────────────────────────────────────────────────────
export const authService = {
  login: async (username, password) => {
    const { data } = await api.post('/api/auth/login', { username, password });
    // Bundling the token onto the user object means AppContext's existing
    // sessionStorage-backed login()/logout() already persists (and clears) it —
    // no separate storage key or plumbing needed.
    return { ...data.user, token: data.token };
  },
};

// ── Role dashboards ───────────────────────────────────────────────────────────
export const dashboardService = {
  patient:  ssn   => api.get(`/api/patient/${ssn}/dashboard`).then(r => r.data),
  doctor:   empId => api.get(`/api/doctor/${empId}/dashboard`).then(r => r.data),
  nurse:    empId => api.get(`/api/nurse/${empId}/dashboard`).then(r => r.data),
};

// ── Admin / generic CRUD ──────────────────────────────────────────────────────
export const adminService = {
  list:   resource       => api.get(`/${resource}`).then(r => r.data),
  get:    (resource, id) => api.get(`/api/admin/${resource}/${id}`).then(r => r.data),
  update: (resource, id, body) => api.put(`/api/admin/${resource}/${id}`, body).then(r => r.data),
  delete: (resource, id)       => api.delete(`/api/admin/${resource}/${id}`).then(r => r.data),

  // POST convenience (using existing spec endpoints)
  addPhysician:  body => api.post('/api/physician/post', body).then(r => r.data),
  addNurse:      body => api.post('/api/nurse', body).then(r => r.data),
  addPatient:    body => api.post('/api/patient', body).then(r => r.data),
  addDepartment: body => api.post('/api/department', body).then(r => r.data),
  addProcedure:  body => api.post('/api/procedure', body).then(r => r.data),
  addMedication: body => api.post('/api/medication', body).then(r => r.data),
  addRoom:       body => api.post('/api/room', body).then(r => r.data),
  addBlock:      body => api.post('/api/block', body).then(r => r.data),
  addUser:       body => api.post('/api/users', body).then(r => r.data),
};

// ── Staff & patient accounts (admin creates logins for existing records) ──────
export const userService = {
  list:   () => api.get('/api/users').then(r => r.data),
  create: body => api.post('/api/users', body).then(r => r.data),
};

// ── Admissions (admit / discharge patients, tracks room availability) ─────────
export const admissionService = {
  list:      () => api.get('/api/stay').then(r => r.data),
  admit:     body            => api.post('/api/stay/admit', body).then(r => r.data),
  discharge: (stayId, stayEnd) => api.put(`/api/stay/discharge/${stayId}`, { StayEnd: stayEnd }).then(r => r.data),
};

// ── Rooms (create + toggle availability) ───────────────────────────────────────
export const roomService = {
  list:               () => api.get('/api/room').then(r => r.data),
  get:                roomNumber => api.get(`/api/room/${roomNumber}`).then(r => r.data),
  create:             body => api.post('/api/room', body).then(r => r.data),
  updateAvailability: (roomNumber, unavailable) =>
    api.put(`/api/room/availability/${roomNumber}`, { Unavailable: unavailable }).then(r => r.data),
};

// ── Billing (create invoices, mark paid) ───────────────────────────────────────
export const billingService = {
  list:        () => api.get('/api/billing').then(r => r.data),
  byPatient:   ssn => api.get(`/api/billing/patient/${ssn}`).then(r => r.data),
  create:      body => api.post('/api/billing', body).then(r => r.data),
  markPaid:    id   => api.put(`/api/billing/${id}/pay`).then(r => r.data),
};

// ── Salaries (admin sets physician compensation) ───────────────────────────────
export const salaryService = {
  list:   () => api.get('/salaries').then(r => r.data),
  upsert: body => api.post('/api/salary', body).then(r => r.data),
};

// ── Prescriptions (doctor prescribes medication) ───────────────────────────────
export const prescriptionService = {
  list:      () => api.get('/api/prescribe').then(r => r.data),
  byPatient: ssn => api.get(`/api/prescribe/patient/${ssn}`).then(r => r.data),
  create:    body => api.post('/api/prescribe', body).then(r => r.data),
};

// ── Procedures performed (doctor records an operation) ─────────────────────────
export const undergoesService = {
  list:   () => api.get('/api/undergoes').then(r => r.data),
  record: body => api.post('/api/undergoes', body).then(r => r.data),
};

// ── On-call nurse scheduling ────────────────────────────────────────────────────
export const onCallService = {
  list:   () => api.get('/api/on_call').then(r => r.data),
  create: body => api.post('/api/on_call', body).then(r => r.data),
  delete: id   => api.delete(`/api/on_call/${id}`).then(r => r.data),
};

// ── Duty shifts (physician/nurse scheduling) ───────────────────────────────────
export const dutyShiftService = {
  list:   () => api.get('/api/duty_shift').then(r => r.data),
  create: body => api.post('/api/duty_shift', body).then(r => r.data),
  delete: id   => api.delete(`/api/duty_shift/${id}`).then(r => r.data),
};

// ── Blocks (building/floor grouping for rooms) ─────────────────────────────────
export const blockService = {
  list:   () => api.get('/api/block').then(r => r.data),
  create: body => api.post('/api/block', body).then(r => r.data),
  delete: id   => api.delete(`/api/block/${id}`).then(r => r.data),
};

// ── Hospital-wide stats (admin overview dashboard) ─────────────────────────────
export const statsService = {
  get: () => api.get('/api/admin/stats').then(r => r.data),
};

// ── Doctor — scoped patient lookup ────────────────────────────────────────────
export const doctorService = {
  patientDetail: (empId, ssn) => api.get(`/api/patient/physician/${empId}/${ssn}`).then(r => r.data),
};

// ── Patient self-service ──────────────────────────────────────────────────────
export const patientService = {
  bookAppointment:  body           => api.post('/api/appointment/', body).then(r => r.data),
  editAppointment:  (id, body)     => api.put(`/api/patient/appointment/${id}`, body).then(r => r.data),
  // Single-record lookup (not the full roster) — safe for a patient to call for
  // themselves, and for the treating doctor/nurse/admin to call for a specific patient.
  get: ssn => api.get(`/api/patient/${ssn}`).then(r => r.data),
};

// ── Single-record lookups used by shared pages (safer than fetching the full list
//    just to find one row — see AppointmentDetail) ─────────────────────────────
export const physicianService = {
  get: empId => api.get(`/api/physician/empid/${empId}`).then(r => r.data),
};

export const appointmentService = {
  get: id => api.get(`/api/appointment/${id}`).then(r => r.data),
};

// ── Reference data ────────────────────────────────────────────────────────────
export const refService = {
  physicians:  () => api.get('/physicians').then(r => r.data),
  nurses:      () => api.get('/nurses').then(r => r.data),
  patients:    () => api.get('/patients').then(r => r.data),
  departments: () => api.get('/api/department/').then(r => r.data),
  procedures:  () => api.get('/api/procedure/').then(r => r.data),
  appointments:() => api.get('/api/appointment').then(r => r.data),
  medications: () => api.get('/medications').then(r => r.data),
  rooms:       () => api.get('/rooms').then(r => r.data),
  salaries:    () => api.get('/salaries').then(r => r.data),
  billing:     () => api.get('/billing').then(r => r.data),
  users:       () => api.get('/users').then(r => r.data),
};