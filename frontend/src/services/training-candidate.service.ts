import { api, apiRequest } from "./api";

export type TrainingCandidateStatus = "APPROVED" | "REJECTED" | "IN_TRAINING" | "CANDIDATE";

export type TrainingCandidateView = {
  id: number;
  documentId: number;
  contentHash: string;
  title: string;
  content: string;
  localResult: unknown | null;
  nemotronResult: unknown | null;
  status: TrainingCandidateStatus;
  reviewedByUserId: number | null;
  createdAt: string;
  reviewedAt: string | null;
};

export async function listTrainingCandidates(
  status: TrainingCandidateStatus = "CANDIDATE",
  limit = 100,
): Promise<TrainingCandidateView[]> {
  return apiRequest<TrainingCandidateView[]>({
    method: "GET",
    url: "/admin/training-candidates",
    params: { status, limit },
  });
}

export async function updateTrainingCandidateStatus(
  candidateId: number,
  status: TrainingCandidateStatus,
): Promise<void> {
  await apiRequest<void>({
    method: "PATCH",
    url: `/admin/training-candidates/${candidateId}`,
    params: { status },
  });
}

export async function exportApprovedTrainingCandidates(): Promise<Blob> {
  const response = await api.get<Blob>("/admin/training-candidates/export", {
    responseType: "blob",
  });
  return response.data;
}
