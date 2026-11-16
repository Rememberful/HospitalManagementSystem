import { useEffect, useState } from "react";
import { refService, billingService } from "../../services/api";
import Loader from "../../components/Loader";
import { isBlank, toNum, fmt } from "../../utils/helpers";

export default function AdminBilling() {
  const [rows, setRows] = useState([]);
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ patient: "", label: "", amount: "" });
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState({ state: "idle", msg: "" });
  const [busyId, setBusyId] = useState(null);

  const load = () => {
    setLoading(true);
    Promise.all([billingService.list(), refService.patients()])
      .then(([bills, pts]) => {
        setRows(bills);
        setPatients(pts);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const addItem = () => {
    if (isBlank(form.label) || isBlank(form.amount)) return;
    setItems((it) => [...it, { type: "charge", label: form.label.trim(), amount: toNum(form.amount) }]);
    setForm((f) => ({ ...f, label: "", amount: "" }));
  };

  const removeItem = (idx) => setItems((it) => it.filter((_, i) => i !== idx));

  const total = items.reduce((sum, it) => sum + (it.amount || 0), 0);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (isBlank(form.patient) || items.length === 0) {
      setStatus({ state: "error", msg: "Select a patient and add at least one line item." });
      return;
    }
    setStatus({ state: "loading", msg: "" });
    try {
      await billingService.create({ patient: toNum(form.patient), items });
      setStatus({ state: "success", msg: "Invoice created." });
      setItems([]);
      setForm({ patient: "", label: "", amount: "" });
      load();
    } catch (err) {
      setStatus({ state: "error", msg: err.message });
    }
  };

  const handleMarkPaid = async (id) => {
    setBusyId(id);
    try {
      await billingService.markPaid(id);
      load();
    } catch (err) {
      alert(err.message);
    } finally {
      setBusyId(null);
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
            Billing
          </h1>
        </div>
      </div>

      <form
        className="mb-7 max-w-[660px] rounded-[16px] border border-[var(--border)] bg-[var(--surface)] p-[26px]"
        onSubmit={handleCreate}
      >
        <div className="mb-[14px] flex items-center gap-[10px] font-[var(--font-d)] text-[15px] font-semibold text-[var(--text)]">
          <span className="h-[6px] w-[6px] shrink-0 rounded-full bg-[var(--accent)]" />
          Create invoice
        </div>

        <div className="mt-[18px] flex flex-col gap-[6px]">
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

        <div className="mt-4 grid grid-cols-[1fr_140px_auto] gap-3 max-[560px]:grid-cols-1">
          <input
            placeholder="Line item, e.g. Consultation"
            value={form.label}
            onChange={set("label")}
            className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
          />
          <input
            type="number"
            placeholder="Amount"
            value={form.amount}
            onChange={set("amount")}
            className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
          />
          <button
            type="button"
            onClick={addItem}
            className="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-[6px] border border-[var(--border-2)] px-[16px] py-[10px] text-[13px] font-semibold text-[var(--text)] transition duration-150 hover:border-[var(--accent-bdr)] hover:text-[var(--accent)]"
          >
            + Add line
          </button>
        </div>

        {items.length > 0 && (
          <div className="mt-4 rounded-[8px] border border-[var(--border)] divide-y divide-[var(--border)]">
            {items.map((it, i) => (
              <div key={i} className="flex items-center justify-between px-3 py-2 text-sm">
                <span className="text-[var(--text)]">{it.label}</span>
                <div className="flex items-center gap-3">
                  <span className="[font-family:var(--font-m)] text-xs text-[var(--text-dim)]">
                    {fmt.currency(it.amount)}
                  </span>
                  <button
                    type="button"
                    onClick={() => removeItem(i)}
                    className="text-xs text-[var(--danger)] hover:underline"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
            <div className="flex items-center justify-between px-3 py-2 text-sm font-semibold">
              <span>Total</span>
              <span className="[font-family:var(--font-m)]">{fmt.currency(total)}</span>
            </div>
          </div>
        )}

        <div className="mt-6 flex flex-wrap items-center gap-3">
          <button
            type="submit"
            className="inline-flex cursor-pointer items-center justify-center gap-2 whitespace-nowrap rounded-[6px] border border-transparent bg-[var(--accent)] px-[18px] py-[10px] text-[14px] font-semibold leading-none tracking-[0.04em] text-white uppercase transition duration-150 active:translate-y-px hover:bg-[var(--accent-b)] disabled:cursor-not-allowed disabled:opacity-50"
            disabled={status.state === "loading"}
          >
            {status.state === "loading" ? "Creating…" : "Create invoice"}
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
                {["Invoice", "Patient SSN", "Total", "Status", "Actions"].map((h) => (
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
              {rows.map((b) => (
                <tr key={b.id} className="last:[&>td]:border-b-0 hover:[&>td]:bg-[var(--surface-2)]">
                  <td className="px-4 py-3 border-b border-[var(--border)] [font-family:var(--font-m)] text-xs text-[var(--text)]">
                    {b.id}
                  </td>
                  <td className="px-4 py-3 border-b border-[var(--border)] [font-family:var(--font-m)] text-xs text-[var(--text)]">
                    …{String(b.patient).slice(-4)}
                  </td>
                  <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">
                    {fmt.currency(b.total)}
                  </td>
                  <td className="px-4 py-3 border-b border-[var(--border)]">
                    <span
                      className={`inline-flex items-center gap-1.25 rounded-[99px] px-2.25 py-0.75 text-[11px] font-semibold tracking-[0.02em] ${
                        b.paid
                          ? "bg-[var(--success-soft)] text-[var(--success)]"
                          : "bg-[var(--warn-soft,rgba(217,119,6,0.12))] text-[var(--warn,#d97706)]"
                      }`}
                    >
                      {b.paid ? "Paid" : "Unpaid"}
                    </span>
                  </td>
                  <td className="px-4 py-3 border-b border-[var(--border)]">
                    {!b.paid && (
                      <button
                        className="inline-flex items-center justify-center gap-2 px-[9px] py-[4px] rounded-[4px] font-medium text-[11.5px] border border-[var(--border-2)] cursor-pointer transition-all duration-150 active:translate-y-px whitespace-nowrap leading-none bg-transparent text-[var(--text)] hover:border-[var(--accent-bdr)] hover:text-[var(--accent)] disabled:opacity-50"
                        onClick={() => handleMarkPaid(b.id)}
                        disabled={busyId === b.id}
                      >
                        {busyId === b.id ? "Marking…" : "Mark paid"}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={5} className="p-6 text-center text-[var(--text-dim)]">
                    No invoices yet.
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
