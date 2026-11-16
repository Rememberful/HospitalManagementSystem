import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { statsService } from "../../services/api";
import Loader from "../../components/Loader";
import { fmt } from "../../utils/helpers";

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    statsService
      .get()
      .then(setStats)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader label="Loading admin panel…" />;

  const tiles = [
    { label: "Physicians", value: stats.totalPhysicians, to: "/admin/physicians", color: "var(--info)" },
    { label: "Nurses", value: stats.totalNurses, to: "/admin/nurses", color: "var(--success)" },
    { label: "Patients", value: stats.totalPatients, to: "/admin/patients", color: "var(--warn)" },
    { label: "Departments", value: stats.totalDepartments, to: "/admin/departments", color: "var(--accent-b)" },
    { label: "Appointments today", value: stats.appointmentsToday, to: "/admin/appointments", color: "var(--danger)" },
    { label: "Active admissions", value: stats.activeAdmissions, to: "/admin/admissions", color: "var(--text-dim)" },
  ];

  const opsTiles = [
    { label: "Rooms occupied", value: `${stats.occupiedRooms} / ${stats.totalRooms}`, to: "/admin/rooms" },
    { label: "Total revenue collected", value: fmt.currency(stats.totalRevenue), to: "/admin/billing" },
    { label: "Unpaid invoices", value: stats.unpaidInvoiceCount, to: "/admin/billing" },
  ];

  return (
    <div>
      <div className="page-head">
        <div>
          <span className="eyebrow">Admin panel</span>
          <h1>Master control</h1>
          <p>Add, edit, or remove any record across the entire system.</p>
        </div>
      </div>

      <div className="stat-grid">
        {tiles.map((t) => (
          <Link key={t.label} to={t.to} className="stat-tile">
            <span
              className="st-dot"
              style={{
                background: t.color,
                boxShadow: `0 0 0 4px ${t.color}22`,
              }}
            />
            <div className="st-label">{t.label}</div>
            <div className="st-value">{t.value}</div>
          </Link>
        ))}
      </div>

      <div className="card" style={{ marginTop: 24 }}>
        <div className="sec-title" style={{ marginBottom: 16 }}>
          <span className="dot" />
          Hospital operations
        </div>
        <div className="stat-grid">
          {opsTiles.map((t) => (
            <Link key={t.label} to={t.to} className="stat-tile">
              <div className="st-label">{t.label}</div>
              <div className="st-value">{t.value}</div>
            </Link>
          ))}
        </div>
      </div>

      <div className="card" style={{ marginTop: 24 }}>
        <div className="sec-title" style={{ marginBottom: 16 }}>
          <span className="dot" />
          Quick actions
        </div>
        <div className="flex-row gap-14">
          <Link to="/admin/physicians" className="btn btn-ghost">+ Add physician</Link>
          <Link to="/admin/nurses" className="btn btn-ghost">+ Add nurse</Link>
          <Link to="/admin/patients" className="btn btn-ghost">+ Add patient</Link>
          <Link to="/admin/admissions" className="btn btn-ghost">+ Admit patient</Link>
          <Link to="/admin/billing" className="btn btn-ghost">+ Create invoice</Link>
          <Link to="/admin/users" className="btn btn-ghost">+ Create account</Link>
        </div>
      </div>
    </div>
  );
}
