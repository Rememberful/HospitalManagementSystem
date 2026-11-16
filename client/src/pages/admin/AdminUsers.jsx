import { useEffect, useState } from "react";
import { refService, userService } from "../../services/api";
import AdminTable from "../../components/AdminTable";
import Loader from "../../components/Loader";
import { isBlank, toNum } from "../../utils/helpers";

const ROLES = ["admin", "doctor", "nurse", "patient"];

export default function AdminUsers() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({
    id: "",
    username: "",
    password: "",
    role: "doctor",
    displayName: "",
    linkId: "",
  });
  const [status, setStatus] = useState({ state: "idle", msg: "" });

  const load = () => {
    setLoading(true);
    refService.users().then(setRows).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const needsLink = form.role !== "admin";

  const handleAdd = async (e) => {
    e.preventDefault();
    if ([form.id, form.username, form.password, form.displayName].some(isBlank)) {
      setStatus({ state: "error", msg: "All fields except the linked record ID are required." });
      return;
    }
    if (needsLink && isBlank(form.linkId)) {
      setStatus({
        state: "error",
        msg: `A ${form.role === "patient" ? "patient SSN" : "employee ID"} is required for a ${form.role} account.`,
      });
      return;
    }
    setStatus({ state: "loading", msg: "" });
    try {
      await userService.create({
        id: toNum(form.id),
        username: form.username.trim(),
        password: form.password,
        role: form.role,
        displayName: form.displayName.trim(),
        ssn: form.role === "patient" ? toNum(form.linkId) : null,
        employeeId: form.role === "doctor" || form.role === "nurse" ? toNum(form.linkId) : null,
      });
      setStatus({ state: "success", msg: "Account created." });
      setForm({ id: "", username: "", password: "", role: "doctor", displayName: "", linkId: "" });
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
            Staff & Patient Accounts
          </h1>
          <p className="mt-1 text-sm text-[var(--text-dim)]">
            Create the login a physician, nurse, or patient uses — after they've already been added as a record.
          </p>
        </div>
      </div>

      <form
        className="mb-7 max-w-[660px] rounded-[16px] border border-[var(--border)] bg-[var(--surface)] p-[26px]"
        onSubmit={handleAdd}
      >
        <div className="mb-[14px] flex items-center gap-[10px] font-[var(--font-d)] text-[15px] font-semibold text-[var(--text)]">
          <span className="h-[6px] w-[6px] shrink-0 rounded-full bg-[var(--accent)]" />
          Create account
        </div>

        <div className="mt-[18px] grid grid-cols-2 gap-4 max-[560px]:grid-cols-1">
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Account ID
            </label>
            <input
              type="number"
              placeholder="e.g. 10"
              value={form.id}
              onChange={set("id")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 placeholder:text-[var(--text-faint)] focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>

          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Role
            </label>
            <select
              value={form.role}
              onChange={set("role")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r[0].toUpperCase() + r.slice(1)}
                </option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Username
            </label>
            <input
              value={form.username}
              onChange={set("username")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 placeholder:text-[var(--text-faint)] focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>

          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Password
            </label>
            <input
              type="text"
              value={form.password}
              onChange={set("password")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 placeholder:text-[var(--text-faint)] focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>

          <div className="col-span-2 flex flex-col gap-[6px] max-[560px]:col-span-1">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Display name
            </label>
            <input
              value={form.displayName}
              onChange={set("displayName")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 placeholder:text-[var(--text-faint)] focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>

          {needsLink && (
            <div className="col-span-2 flex flex-col gap-[6px] max-[560px]:col-span-1">
              <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
                {form.role === "patient" ? "Patient SSN" : "Employee ID"} — must already exist
              </label>
              <input
                type="number"
                value={form.linkId}
                onChange={set("linkId")}
                className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 placeholder:text-[var(--text-faint)] focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
              />
            </div>
          )}
        </div>

        <div className="mt-6 flex flex-wrap items-center gap-3">
          <button
            type="submit"
            className="inline-flex cursor-pointer items-center justify-center gap-2 whitespace-nowrap rounded-[6px] border border-transparent bg-[var(--accent)] px-[18px] py-[10px] text-[14px] font-semibold leading-none tracking-[0.04em] text-white uppercase transition duration-150 active:translate-y-px hover:bg-[var(--accent-b)] disabled:cursor-not-allowed disabled:opacity-50"
            disabled={status.state === "loading"}
          >
            {status.state === "loading" ? "Creating…" : "+ Create account"}
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
        <AdminTable
          resource="users"
          idField="id"
          rows={rows}
          onRefresh={load}
          columns={[
            { key: "id", label: "ID", mono: true },
            { key: "username", label: "Username" },
            { key: "role", label: "Role" },
            { key: "displayName", label: "Display name" },
          ]}
          editFields={[
            { key: "displayName", label: "Display name" },
            { key: "password", label: "Password" },
          ]}
        />
      )}
    </div>
  );
}
