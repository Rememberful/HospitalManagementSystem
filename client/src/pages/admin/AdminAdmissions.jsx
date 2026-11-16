import { useEffect, useState } from "react";
import { refService, admissionService } from "../../services/api";
import Loader from "../../components/Loader";
import { isBlank, toNum } from "../../utils/helpers";

export default function AdminAdmissions() {
  const [rows, setRows] = useState([]);
  const [patients, setPatients] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ stayId: "", patient: "", room: "", stayStart: "" });
  const [status, setStatus] = useState({ state: "idle", msg: "" });
  const [busyStay, setBusyStay] = useState(null);

  const load = () => {
    setLoading(true);
    Promise.all([admissionService.list(), refService.patients(), refService.rooms()])
      .then(([stays, pts, rms]) => {
        setRows(stays);
        setPatients(pts);
        setRooms(rms.filter((r) => !r.Unavailable));
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const handleAdmit = async (e) => {
    e.preventDefault();
    if ([form.stayId, form.patient, form.room, form.stayStart].some(isBlank)) {
      setStatus({ state: "error", msg: "All fields are required." });
      return;
    }
    setStatus({ state: "loading", msg: "" });
    try {
      await admissionService.admit({
        stayId: toNum(form.stayId),
        patient: toNum(form.patient),
        room: toNum(form.room),
        stayStart: form.stayStart,
      });
      setStatus({ state: "success", msg: "Patient admitted." });
      setForm({ stayId: "", patient: "", room: "", stayStart: "" });
      load();
    } catch (err) {
      setStatus({ state: "error", msg: err.message });
    }
  };

  const handleDischarge = async (stay) => {
    const today = new Date().toISOString().slice(0, 10);
    setBusyStay(stay.StayID);
    try {
      await admissionService.discharge(stay.StayID, today);
      load();
    } catch (err) {
      alert(err.message);
    } finally {
      setBusyStay(null);
    }
  };

  const activeStays = rows.filter((s) => !s.StayEnd);
  const pastStays = rows.filter((s) => s.StayEnd);

  return (
    <div>
      <div className="mb-[26px] flex flex-wrap items-end justify-between gap-4">
        <div>
          <span className="mb-[6px] block font-mono text-[11px] uppercase tracking-[0.14em] text-[var(--accent)]">
            Admin / manage
          </span>
          <h1 className="m-0 font-[var(--font-d)] text-[28px] font-bold tracking-[-0.01em] text-[var(--text)]">
            Admissions
          </h1>
        </div>
      </div>

      <form
        className="mb-7 max-w-[660px] rounded-[16px] border border-[var(--border)] bg-[var(--surface)] p-[26px]"
        onSubmit={handleAdmit}
      >
        <div className="mb-[14px] flex items-center gap-[10px] font-[var(--font-d)] text-[15px] font-semibold text-[var(--text)]">
          <span className="h-[6px] w-[6px] shrink-0 rounded-full bg-[var(--accent)]" />
          Admit a patient
        </div>

        <div className="mt-[18px] grid grid-cols-2 gap-4 max-[560px]:grid-cols-1">
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Stay ID
            </label>
            <input
              type="number"
              value={form.stayId}
              onChange={set("stayId")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Admission date
            </label>
            <input
              type="date"
              value={form.stayStart}
              onChange={set("stayStart")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>
          <div className="col-span-2 flex flex-col gap-[6px] max-[560px]:col-span-1">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Patient
            </label>
            <select
              value={form.patient}
              onChange={set("patient")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            >
              <option value="">Select patient…</option>
              {patients.map((p) => (
                <option key={p.SSN} value={p.SSN}>
                  {p.Name} (SSN …{String(p.SSN).slice(-4)})
                </option>
              ))}
            </select>
          </div>
          <div className="col-span-2 flex flex-col gap-[6px] max-[560px]:col-span-1">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Room (available only)
            </label>
            <select
              value={form.room}
              onChange={set("room")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            >
              <option value="">Select room…</option>
              {rooms.map((r) => (
                <option key={r.RoomNumber} value={r.RoomNumber}>
                  Room {r.RoomNumber} — {r.RoomType} (Floor {r.BlockFloor}, Block {r.BlockCode})
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mt-6 flex flex-wrap items-center gap-3">
          <button
            type="submit"
            className="inline-flex cursor-pointer items-center justify-center gap-2 whitespace-nowrap rounded-[6px] border border-transparent bg-[var(--accent)] px-[18px] py-[10px] text-[14px] font-semibold leading-none tracking-[0.04em] text-white uppercase transition duration-150 active:translate-y-px hover:bg-[var(--accent-b)] disabled:cursor-not-allowed disabled:opacity-50"
            disabled={status.state === "loading"}
          >
            {status.state === "loading" ? "Admitting…" : "+ Admit patient"}
          </button>
        </div>

        {status.msg && (
          <div
            className={`mt-[14px] rounded-[6px] border px-[14px] py-[10px] text-[13px] ${
              status.state === "success"
                ? "border-[rgba(22,163,74,.25)] bg-[var(--success-soft)] text-[var(--success)]"
                : "border-[rgba(220,38,38,.25)] bg-[var(--danger-soft)] text-[var(--danger)]"
            }`}
          >
            {status.msg}
          </div>
        )}
      </form>

      {loading ? (
        <Loader />
      ) : (
        <>
          <div className="mb-[14px] flex items-center gap-[10px] font-[var(--font-d)] text-[15px] font-semibold text-[var(--text)]">
            Currently admitted ({activeStays.length})
          </div>
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius-lg)] overflow-hidden overflow-x-auto mb-8">
            <table className="w-full border-collapse text-[13.5px]">
              <thead>
                <tr>
                  {["Stay ID", "Patient SSN", "Room", "Admitted", "Actions"].map((h) => (
                    <th
                      key={h}
                      className="text-left px-4 py-[11px] bg-[var(--surface-2)] text-[var(--text-dim)] text-[11px] uppercase tracking-[0.07em] border-b border-[var(--border)] whitespace-nowrap"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {activeStays.map((s) => (
                  <tr key={s.StayID} className="last:[&>td]:border-b-0 hover:[&>td]:bg-[var(--surface-2)]">
                    <td className="px-4 py-3 border-b border-[var(--border)] [font-family:var(--font-m)] text-xs text-[var(--text)]">
                      {s.StayID}
                    </td>
                    <td className="px-4 py-3 border-b border-[var(--border)] [font-family:var(--font-m)] text-xs text-[var(--text)]">
                      …{String(s.Patient).slice(-4)}
                    </td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{s.Room}</td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{s.StayStart}</td>
                    <td className="px-4 py-3 border-b border-[var(--border)]">
                      <button
                        className="inline-flex items-center justify-center gap-2 px-[9px] py-[4px] rounded-[4px] font-medium text-[11.5px] border cursor-pointer transition-all duration-150 active:translate-y-px whitespace-nowrap leading-none bg-[var(--danger-soft)] text-[var(--danger)] border-[rgba(220,38,38,0.25)] hover:bg-[var(--danger)] hover:text-white disabled:opacity-50"
                        onClick={() => handleDischarge(s)}
                        disabled={busyStay === s.StayID}
                      >
                        {busyStay === s.StayID ? "Discharging…" : "Discharge"}
                      </button>
                    </td>
                  </tr>
                ))}
                {activeStays.length === 0 && (
                  <tr>
                    <td colSpan={5} className="p-6 text-center text-[var(--text-dim)]">
                      No patients currently admitted.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="mb-[14px] flex items-center gap-[10px] font-[var(--font-d)] text-[15px] font-semibold text-[var(--text)]">
            Discharge history ({pastStays.length})
          </div>
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius-lg)] overflow-hidden overflow-x-auto">
            <table className="w-full border-collapse text-[13.5px]">
              <thead>
                <tr>
                  {["Stay ID", "Patient SSN", "Room", "Admitted", "Discharged"].map((h) => (
                    <th
                      key={h}
                      className="text-left px-4 py-[11px] bg-[var(--surface-2)] text-[var(--text-dim)] text-[11px] uppercase tracking-[0.07em] border-b border-[var(--border)] whitespace-nowrap"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {pastStays.map((s) => (
                  <tr key={s.StayID} className="last:[&>td]:border-b-0 hover:[&>td]:bg-[var(--surface-2)]">
                    <td className="px-4 py-3 border-b border-[var(--border)] [font-family:var(--font-m)] text-xs text-[var(--text)]">
                      {s.StayID}
                    </td>
                    <td className="px-4 py-3 border-b border-[var(--border)] [font-family:var(--font-m)] text-xs text-[var(--text)]">
                      …{String(s.Patient).slice(-4)}
                    </td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{s.Room}</td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{s.StayStart}</td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{s.StayEnd}</td>
                  </tr>
                ))}
                {pastStays.length === 0 && (
                  <tr>
                    <td colSpan={5} className="p-6 text-center text-[var(--text-dim)]">
                      No discharge history yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
