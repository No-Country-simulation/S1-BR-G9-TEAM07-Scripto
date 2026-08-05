import { apiRequest } from "./api";

export type ReportReason =
  | "SPAM"
  | "HARASSMENT"
  | "HATEFUL_CONTENT"
  | "ILLEGAL_CONTENT"
  | "COPYRIGHT"
  | "MISINFORMATION"
  | "OTHER";

export type ReportStatus = "OPEN" | "DISMISSED" | "ACTIONED";

export type ReportRequestDTO = {
  reason: ReportReason;
  details?: string;
};

export type ReportResponseDTO = {
  id: number;
  documentId: number;
  reporterId: number;
  reason: ReportReason;
  details: string | null;
  status: ReportStatus;
  createdAt: string;
  reviewedAt: string | null;
};

export type ReportReviewDTO = {
  status: ReportStatus;
  blockDocument: boolean;
};

export async function reportDocument(
  documentId: number,
  request: ReportRequestDTO,
): Promise<ReportResponseDTO> {
  return apiRequest<ReportResponseDTO>({
    method: "POST",
    url: `/document/${documentId}/reports`,
    data: request,
  });
}

export async function listOpenReports(): Promise<ReportResponseDTO[]> {
  return apiRequest<ReportResponseDTO[]>({ method: "GET", url: "/admin/reports" });
}

export async function reviewReport(
  reportId: number,
  request: ReportReviewDTO,
): Promise<ReportResponseDTO> {
  return apiRequest<ReportResponseDTO>({
    method: "PATCH",
    url: `/admin/reports/${reportId}`,
    data: request,
  });
}
