import { listOpenReports } from "./report.service";

export type AdminMetrics = {
  users: number | null;
  reportsOpen: number;
  documents: number | null;
  suspendedUsers: number | null;
};

export async function getAdminMetrics(): Promise<AdminMetrics> {
  const reports = await listOpenReports();
  return {
    users: null,
    reportsOpen: reports.length,
    documents: null,
    suspendedUsers: null,
  };
}
