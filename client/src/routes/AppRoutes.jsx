import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AppContext';

// Public
import Landing  from '../pages/Landing';
import Login    from '../pages/Login';
import ErrorPage from '../pages/ErrorPage';

// Patient
import PatientDashboard    from '../pages/patient/PatientDashboard';
import PatientAppointments from '../pages/patient/PatientAppointments';
import { PatientBilling, PatientProfile, BillDetail } from '../pages/patient/PatientOther';

// Doctor
import DoctorDashboard from '../pages/doctor/DoctorDashboard';
import DoctorSalary    from '../pages/doctor/DoctorSalary';
import DoctorShifts    from '../pages/doctor/DoctorShifts';
import { DoctorPatients, DoctorOperations, DoctorPatientDetail, OperationDetail } from '../pages/doctor/DoctorPatients';

// Nurse
import { NurseDashboard, NurseAssignments, NurseRooms, NurseShifts, RoomDetail } from '../pages/nurse/NursePages';

// Shared
import AppointmentDetail from '../pages/shared/AppointmentDetail';
import ShiftDetail       from '../pages/shared/ShiftDetail';

// Admin
import AdminDashboard    from '../pages/admin/AdminDashboard';
import AdminPhysicians   from '../pages/admin/AdminPhysicians';
import AdminNurses       from '../pages/admin/AdminNurses';
import AdminPatients     from '../pages/admin/AdminPatients';
import AdminAppointments from '../pages/admin/AdminAppointments';
import { AdminDepartments, AdminProcedures } from '../pages/admin/AdminDeptProc';
import AdminRecordDetail from '../pages/admin/AdminRecordDetail';
import AdminMedications  from '../pages/admin/AdminMedications';
import AdminUsers        from '../pages/admin/AdminUsers';
import AdminRooms        from '../pages/admin/AdminRooms';
import AdminAdmissions   from '../pages/admin/AdminAdmissions';
import AdminBilling      from '../pages/admin/AdminBilling';
import AdminSalaries     from '../pages/admin/AdminSalaries';

// `roles` (optional) restricts a route to specific roles. A signed-in user with the wrong role
// is sent to /app (which routes them to their own dashboard) rather than /login — they ARE
// authenticated, just not allowed here. This is UX + defence in depth only: the backend
// independently enforces the same rules, so hiding a page here is never the only protection.
function RequireAuth({ children, roles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/app" replace />;
  return children;
}

function ShiftsSwitch() {
  const { user } = useAuth();
  if (user?.role === 'nurse') return <NurseShifts />;
  return <DoctorShifts />;
}

function RoleSwitch() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  switch (user.role) {
    case 'admin':   return <AdminDashboard />;
    case 'doctor':  return <DoctorDashboard />;
    case 'nurse':   return <NurseDashboard />;
    case 'patient': return <PatientDashboard />;
    default:        return <Navigate to="/login" replace />;
  }
}

export default function AppRoutes() {
  return (
    <Routes>
    
      <Route path="/"      element={<Landing />} />
      <Route path="/login" element={<Login />} />

      <Route path="/app" element={<RequireAuth><RoleSwitch /></RequireAuth>} />

   
      <Route path="/appointments"    element={<RequireAuth roles={['patient']}><PatientAppointments /></RequireAuth>} />
      <Route path="/appointments/:id" element={<RequireAuth roles={['patient','doctor','admin']}><AppointmentDetail /></RequireAuth>} />
      <Route path="/billing"      element={<RequireAuth roles={['patient']}><PatientBilling /></RequireAuth>} />
      <Route path="/billing/:id"  element={<RequireAuth roles={['patient']}><BillDetail /></RequireAuth>} />
      <Route path="/profile"      element={<RequireAuth roles={['patient']}><PatientProfile /></RequireAuth>} />

      <Route path="/patients"     element={<RequireAuth roles={['doctor']}><DoctorPatients /></RequireAuth>} />
      <Route path="/patients/:ssn" element={<RequireAuth roles={['doctor']}><DoctorPatientDetail /></RequireAuth>} />
      <Route path="/operations"   element={<RequireAuth roles={['doctor']}><DoctorOperations /></RequireAuth>} />
      <Route path="/operations/:id" element={<RequireAuth roles={['doctor']}><OperationDetail /></RequireAuth>} />
      <Route path="/salary"       element={<RequireAuth roles={['doctor']}><DoctorSalary /></RequireAuth>} />
      <Route path="/shifts"       element={<RequireAuth roles={['doctor','nurse']}><ShiftsSwitch /></RequireAuth>} />
      <Route path="/shifts/:id"   element={<RequireAuth roles={['doctor','nurse']}><ShiftDetail /></RequireAuth>} />

   
      <Route path="/assignments" element={<RequireAuth roles={['nurse']}><NurseAssignments /></RequireAuth>} />
      <Route path="/rooms"       element={<RequireAuth roles={['nurse']}><NurseRooms /></RequireAuth>} />
      <Route path="/rooms/:id"   element={<RequireAuth roles={['nurse']}><RoomDetail /></RequireAuth>} />

      <Route path="/admin/physicians"   element={<RequireAuth roles={['admin']}><AdminPhysicians /></RequireAuth>} />
      <Route path="/admin/nurses"       element={<RequireAuth roles={['admin']}><AdminNurses /></RequireAuth>} />
      <Route path="/admin/patients"     element={<RequireAuth roles={['admin']}><AdminPatients /></RequireAuth>} />
      <Route path="/admin/departments"  element={<RequireAuth roles={['admin']}><AdminDepartments /></RequireAuth>} />
      <Route path="/admin/procedures"   element={<RequireAuth roles={['admin']}><AdminProcedures /></RequireAuth>} />
      <Route path="/admin/appointments" element={<RequireAuth roles={['admin']}><AdminAppointments /></RequireAuth>} />
      <Route path="/admin/medications"  element={<RequireAuth roles={['admin']}><AdminMedications /></RequireAuth>} />
      <Route path="/admin/users"        element={<RequireAuth roles={['admin']}><AdminUsers /></RequireAuth>} />
      <Route path="/admin/rooms"        element={<RequireAuth roles={['admin']}><AdminRooms /></RequireAuth>} />
      <Route path="/admin/admissions"   element={<RequireAuth roles={['admin']}><AdminAdmissions /></RequireAuth>} />
      <Route path="/admin/billing"      element={<RequireAuth roles={['admin']}><AdminBilling /></RequireAuth>} />
      <Route path="/admin/salaries"     element={<RequireAuth roles={['admin']}><AdminSalaries /></RequireAuth>} />


      <Route path="/admin/:resource/:id" element={<RequireAuth roles={['admin']}><AdminRecordDetail /></RequireAuth>} />

      <Route path="*" element={<ErrorPage code={404} />} />
    </Routes>
  );
}