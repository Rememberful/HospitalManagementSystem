import { useEffect, useState } from "react";
import { refService, salaryService } from "../../services/api";
import Loader from "../../components/Loader";
import { isBlank, toNum, fmt } from "../../utils/helpers";

export default function AdminSalaries() {
  const [rows, setRows] = useState([]);
  const [physicians, setPhysicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ physician: "", baseSalary: "", commissionRate: "" });
  const [status, setStatus] = useState({ state: "idle", msg: "" });

  const load = () => {
    setLoading(true);
    Promise.all([salaryService.list(), refService.physicians()])
      .then(([sal, docs]) => {
        setRows(sal);
        setPhysicians(docs);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const nameFor = (empId) => physicians.find((p) => p.EmployeeID === empId)?.Name || `#${empId}`;

  const handleSave = async (e) => {
    e.preventDefault();
    if ([form.physician, form.baseSalary, form.commissionRate].some(isBlank)) {
      setStatus({ state: "error", msg: "All fields are required." });
      return;
    }
    setStatus({ state: "loading", msg: "" });
    try {
      await salaryService.upsert({
        physician: toNum(form.physician),
        baseSalary: toNum(form.baseSalary),
        commissionRate: toNum(form.commissionRate),
      });
      setStatus({ state: "success", msg: "Salary saved." });
      setForm({ physician: "", baseSalary: "", commissionRate: "" });
      load();
    } catch (err) {
      setStatus({ state: "error", msg: err.message });
    }
  };

  return (
    <div>
      <div className="mb-[26px] flex flex-wrap items-end justify-between gap-4">
        <div>
          <span className="mb-[6px] block font-mono text-[11px] uppercase tracking-[0.14em] text-[var(--accent)]">
            Admin / manage
          </span>
          <h1 className="m-0 font-[var(--font-d)] text-[28px] font-bold tracking-[-0.01em] text-[var(--text)]">
            Salaries
          </h1>
          <p className="mt-1 text-sm text-[var(--text-dim)]">
            Set a physician's base salary and commission rate. Saving again for the same physician updates it.
          </p>
        </div>
      </div>

      <form
        className="mb-7 max-w-[660px] rounded-[16px] border border-[var(--border)] bg-[var(--surface)] p-[26px]"
        onSubmit={handleSave}
      >
        <div className="mb-[14px] flex items-center gap-[10px] font-[var(--font-d)] text-[15px] font-semibold text-[var(--text)]">
          <span className="h-[6px] w-[6px] shrink-0 rounded-full bg-[var(--accent)]" />
          Set salary
        </div>

        <div className="mt-[18px] grid grid-cols-2 gap-4 max-[560px]:grid-cols-1">
          <div className="col-span-2 flex flex-col gap-[6px] max-[560px]:col-span-1">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Physician
            </label>
            <select
              value={form.physician}
              onChange={set("physician")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            >
              <option value="">Select physician…</option>
              {physicians.map((p) => (
                <option key={p.EmployeeID} value={p.EmployeeID}>
                  {p.Name} ({p.Position})
                </option>
              ))}
            </select>
          </div>
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Base salary (annual)
            </label>
            <input
              type="number"
              value={form.baseSalary}
              onChange={set("baseSalary")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Commission rate (0–1)
            </label>
            <input
              type="number"
              step="0.01"
              placeholder="e.g. 0.04"
              value={form.commissionRate}
              onChange={set("commissionRate")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>
        </div>

        <div className="mt-6 flex flex-wrap items-center gap-3">
          <button
            type="submit"
            className="inline-flex cursor-pointer items-center justify-center gap-2 whitespace-nowrap rounded-[6px] border border-transparent bg-[var(--accent)] px-[18px] py-[10px] text-[14px] font-semibold leading-none tracking-[0.04em] text-white uppercase transition duration-150 active:translate-y-px hover:bg-[var(--accent-b)] disabled:cursor-not-allowed disabled:opacity-50"
            disabled={status.state === "loading"}
          >
            {status.state === "loading" ? "Saving…" : "Save salary"}
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
        <div className="bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius-lg)] overflow-hidden overflow-x-auto">
          <table className="w-full border-collapse text-[13.5px]">
            <thead>
              <tr>
                {["Physician", "Base salary", "Commission rate"].map((h) => (
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
              {rows.map((s) => (
                <tr key={s.physician} className="last:[&>td]:border-b-0 hover:[&>td]:bg-[var(--surface-2)]">
                  <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">
                    {nameFor(s.physician)}
                  </td>
                  <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">
                    {fmt.currency(s.baseSalary)}
                  </td>
                  <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">
                    {(s.commissionRate * 100).toFixed(1)}%
                  </td>
                </tr>
              ))}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={3} className="p-6 text-center text-[var(--text-dim)]">
                    No salaries set yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
