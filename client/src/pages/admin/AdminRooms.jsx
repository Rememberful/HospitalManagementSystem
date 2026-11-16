import { useEffect, useState } from "react";
import { refService, roomService } from "../../services/api";
import Loader from "../../components/Loader";
import { isBlank, toNum } from "../../utils/helpers";
import { usePagination } from "../../hooks/usePagination";
import Pagination from "../../components/Pagination";

export default function AdminRooms() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ RoomNumber: "", RoomType: "Single", BlockFloor: "", BlockCode: "" });
  const [status, setStatus] = useState({ state: "idle", msg: "" });
  const [busyRoom, setBusyRoom] = useState(null);
  const pagination = usePagination(rows, 8);

  const load = () => {
    setLoading(true);
    refService.rooms().then(setRows).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const handleAdd = async (e) => {
    e.preventDefault();
    if ([form.RoomNumber, form.RoomType, form.BlockFloor, form.BlockCode].some(isBlank)) {
      setStatus({ state: "error", msg: "All fields are required." });
      return;
    }
    setStatus({ state: "loading", msg: "" });
    try {
      await roomService.create({
        RoomNumber: toNum(form.RoomNumber),
        RoomType: form.RoomType,
        BlockFloor: toNum(form.BlockFloor),
        BlockCode: toNum(form.BlockCode),
      });
      setStatus({ state: "success", msg: "Room added." });
      setForm({ RoomNumber: "", RoomType: "Single", BlockFloor: "", BlockCode: "" });
      load();
    } catch (err) {
      setStatus({ state: "error", msg: err.message });
    }
  };

  const toggleAvailability = async (room) => {
    setBusyRoom(room.RoomNumber);
    try {
      await roomService.updateAvailability(room.RoomNumber, room.Unavailable ? 0 : 1);
      load();
    } catch (err) {
      alert(err.message);
    } finally {
      setBusyRoom(null);
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
            Rooms
          </h1>
        </div>
      </div>

      <form
        className="mb-7 max-w-[660px] rounded-[16px] border border-[var(--border)] bg-[var(--surface)] p-[26px]"
        onSubmit={handleAdd}
      >
        <div className="mb-[14px] flex items-center gap-[10px] font-[var(--font-d)] text-[15px] font-semibold text-[var(--text)]">
          <span className="h-[6px] w-[6px] shrink-0 rounded-full bg-[var(--accent)]" />
          Add room
        </div>

        <div className="mt-[18px] grid grid-cols-2 gap-4 max-[560px]:grid-cols-1">
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Room number
            </label>
            <input
              type="number"
              value={form.RoomNumber}
              onChange={set("RoomNumber")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Room type
            </label>
            <select
              value={form.RoomType}
              onChange={set("RoomType")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            >
              {["Single", "Double", "ICU", "Operating Theater", "Ward"].map((t) => (
                <option key={t}>{t}</option>
              ))}
            </select>
          </div>
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Block floor
            </label>
            <input
              type="number"
              value={form.BlockFloor}
              onChange={set("BlockFloor")}
              className="rounded-[6px] border border-[var(--border)] bg-[var(--surface)] px-[13px] py-[10px] text-[14px] text-[var(--text)] outline-none transition duration-150 focus:border-[var(--accent)] focus:bg-[var(--surface-2)]"
            />
          </div>
          <div className="flex flex-col gap-[6px]">
            <label className="text-[12px] font-semibold uppercase tracking-[0.04em] text-[var(--text-dim)]">
              Block code
            </label>
            <input
              type="number"
              value={form.BlockCode}
              onChange={set("BlockCode")}
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
            {status.state === "loading" ? "Adding…" : "+ Add room"}
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
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-[var(--radius-lg)] overflow-hidden overflow-x-auto">
            <table className="w-full border-collapse text-[13.5px]">
              <thead>
                <tr>
                  {["Room #", "Type", "Floor", "Block", "Status", "Actions"].map((h) => (
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
                {pagination.pageItems.map((r) => (
                  <tr key={r.RoomNumber} className="last:[&>td]:border-b-0 hover:[&>td]:bg-[var(--surface-2)]">
                    <td className="px-4 py-3 border-b border-[var(--border)] [font-family:var(--font-m)] text-xs text-[var(--text)]">
                      {r.RoomNumber}
                    </td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{r.RoomType}</td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{r.BlockFloor}</td>
                    <td className="px-4 py-3 border-b border-[var(--border)] text-[var(--text)]">{r.BlockCode}</td>
                    <td className="px-4 py-3 border-b border-[var(--border)]">
                      <span
                        className={`inline-flex items-center gap-1.25 rounded-[99px] px-2.25 py-0.75 text-[11px] font-semibold tracking-[0.02em] ${
                          r.Unavailable
                            ? "bg-[var(--danger-soft)] text-[var(--danger)]"
                            : "bg-[var(--success-soft)] text-[var(--success)]"
                        }`}
                      >
                        {r.Unavailable ? "Occupied" : "Available"}
                      </span>
                    </td>
                    <td className="px-4 py-3 border-b border-[var(--border)]">
                      <button
                        className="inline-flex items-center justify-center gap-2 px-[9px] py-[4px] rounded-[4px] font-medium text-[11.5px] border border-[var(--border-2)] cursor-pointer transition-all duration-150 active:translate-y-px whitespace-nowrap leading-none bg-transparent text-[var(--text)] hover:border-[var(--accent-bdr)] hover:text-[var(--accent)] disabled:opacity-50"
                        onClick={() => toggleAvailability(r)}
                        disabled={busyRoom === r.RoomNumber}
                      >
                        Mark {r.Unavailable ? "available" : "occupied"}
                      </button>
                    </td>
                  </tr>
                ))}
                {rows.length === 0 && (
                  <tr>
                    <td colSpan={6} className="p-6 text-center text-[var(--text-dim)]">
                      No rooms yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          <Pagination {...pagination} pageSize={8} />
        </>
      )}
    </div>
  );
}
