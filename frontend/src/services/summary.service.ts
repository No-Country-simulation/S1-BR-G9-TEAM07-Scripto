import { apiRequest } from "./api";

export type SummaryResponseDTO = {
  documentId: number;
  summary: string;
  provider: string;
  model: string;
  cached: boolean;
  remainingGenerationsToday: number;
  createdAt: string;
};

export async function summarizeDocument(documentId: number): Promise<SummaryResponseDTO> {
  return apiRequest<SummaryResponseDTO>({
    method: "POST",
    url: `/document/${documentId}/summary`,
  });
}
