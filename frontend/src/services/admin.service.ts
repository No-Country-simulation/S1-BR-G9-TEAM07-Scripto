import { sleep } from "./api";

export type Report = {
  id: string;
  docId: string;
  docTitle: string;
  reason: string;
  note?: string;
  status: "PENDING" | "DISMISSED" | "RESOLVED";
  createdAt: string;
};

const REPORTS_KEY = "scripto-reports";
const SUSP_KEY = "scripto-suspended-users";

function read<T>(k: string): T[] {
  if (typeof window === "undefined") return [];
  try { return JSON.parse(localStorage.getItem(k) || "[]"); } catch { return []; }
}
const write = (k: string, v: unknown) => localStorage.setItem(k, JSON.stringify(v));

export async function createReport(input: { docId: string; docTitle: string; reason: string; note?: string }) {
  await sleep(200);
  const r: Report = { id: crypto.randomUUID(), status: "PENDING", createdAt: new Date().toISOString(), ...input };
  const all = read<Report>(REPORTS_KEY); all.push(r); write(REPORTS_KEY, all);
  return r;
}

export async function listReports() { await sleep(150); return read<Report>(REPORTS_KEY); }

export async function resolveReport(id: string, action: "DISMISSED" | "RESOLVED") {
  const all = read<Report>(REPORTS_KEY);
  const r = all.find(x => x.id === id); if (r) r.status = action;
  write(REPORTS_KEY, all);
}

export function isSuspended(userId: string) {
  return read<string>(SUSP_KEY).includes(userId);
}
export function toggleSuspend(userId: string) {
  const list = read<string>(SUSP_KEY);
  const next = list.includes(userId) ? list.filter(x => x !== userId) : [...list, userId];
  write(SUSP_KEY, next);
}

export async function metrics() {
  await sleep(150);
  const users = JSON.parse(localStorage.getItem("scripto-users") || "[]");
  const docs = JSON.parse(localStorage.getItem("scripto-docs") || "[]");
  const reports = read<Report>(REPORTS_KEY);
  return {
    users: users.length,
    suspended: read<string>(SUSP_KEY).length,
    docs: docs.length,
    reportsPending: reports.filter(r => r.status === "PENDING").length,
  };
}
