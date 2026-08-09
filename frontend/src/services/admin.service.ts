import { apiRequest } from "./api";
import type { ReportReason, ReportStatus } from "./report.service";

export type AdminMetrics = {
  totalUsers: number;
  suspendedUsers: number;
  totalDocuments: number;
  openReports: number;
  closedReports: number;
};

export type AdminAccountStatus = "ACTIVE" | "BANNED" | "PENDING_DELETION";
export type AdminRole = "USER" | "ADMIN";

export type AdminUser = {
  id: number;
  fullName: string;
  email: string;
  cpf: string;
  role: AdminRole;
  accountStatus: AdminAccountStatus;
  documentCount: number;
  reportsMade: number;
  reportsReceived: number;
};

export type AdminUserUpdate = {
  fullName?: string;
  email?: string;
  role?: AdminRole;
  newPassword?: string;
};

export type DocumentStatus = "PENDING" | "PROCESSING" | "PROCESSED" | "ERROR";
export type DocumentVisibility = "PUBLIC" | "PRIVATE";
export type ModerationStatus = "APPROVED" | "PENDING" | "BLOCKED";

export type AdminDocument = {
  id: number;
  title: string;
  ownerId: number;
  ownerName: string;
  status: DocumentStatus;
  visibility: DocumentVisibility;
  moderationStatus: ModerationStatus;
  reportCount: number;
  createdAt: string;
};

export type AdminDocumentDetail = AdminDocument & {
  content: string;
  updatedAt: string;
};

export type AdminDocumentUpdate = {
  visibility?: DocumentVisibility;
  blocked?: boolean;
};

export type AdminReport = {
  id: number;
  documentId: number;
  documentTitle: string;
  ownerId: number;
  ownerName: string;
  reporterId: number | null;
  reporterName: string;
  reason: ReportReason;
  details: string | null;
  status: ReportStatus;
  createdAt: string;
  reviewedAt: string | null;
};

export function getAdminMetrics(): Promise<AdminMetrics> {
  return apiRequest<AdminMetrics>({ method: "GET", url: "/admin/dashboard" });
}

export function listAdminUsers(): Promise<AdminUser[]> {
  return apiRequest<AdminUser[]>({ method: "GET", url: "/admin/users" });
}

export function updateAdminUser(userId: number, request: AdminUserUpdate): Promise<AdminUser> {
  return apiRequest<AdminUser>({ method: "PATCH", url: `/admin/users/${userId}`, data: request });
}

export function setAdminUserBanned(userId: number, banned: boolean): Promise<AdminUser> {
  return apiRequest<AdminUser>({ method: "PATCH", url: `/admin/users/${userId}/ban`, data: { banned } });
}

export function listAdminDocuments(): Promise<AdminDocument[]> {
  return apiRequest<AdminDocument[]>({ method: "GET", url: "/admin/documents" });
}

export function getAdminDocument(documentId: number): Promise<AdminDocumentDetail> {
  return apiRequest<AdminDocumentDetail>({ method: "GET", url: `/admin/documents/${documentId}` });
}

export function updateAdminDocument(documentId: number, request: AdminDocumentUpdate): Promise<AdminDocument> {
  return apiRequest<AdminDocument>({ method: "PATCH", url: `/admin/documents/${documentId}`, data: request });
}

export function listAdminReports(status?: ReportStatus): Promise<AdminReport[]> {
  return apiRequest<AdminReport[]>({ method: "GET", url: "/admin/reports/details", params: status ? { status } : undefined });
}
