import { apiRequest } from "./api";
import type { AIAnalysisResultDTO, Level } from "./classification.service";

export type Status = "PENDING" | "PROCESSING" | "PROCESSED" | "ERROR";
export type Visibility = "PRIVATE" | "PUBLIC";
export type ModerationStatus = "APPROVED" | "PENDING" | "BLOCKED";

export type DocumentRequestDTO = {
  title: string;
  content: string;
  visibility?: Visibility;
  externalAiAllowed?: boolean;
  trainingUseAllowed: boolean;
  usageTermsAccepted: boolean;
};

export type DocumentResponseDTO = {
  documentId: number;
  title: string;
  content: string;
  status: Status;
  visibility: Visibility;
  moderationStatus: ModerationStatus;
  externalAiAllowed: boolean;
  trainingUseAllowed: boolean;
  analysis: AIAnalysisResultDTO | null;
  createdAt: string;
  updatedAt: string | null;
};

export type DocumentListDTO = {
  documentId: number;
  title: string;
  status: Status;
  visibility: Visibility;
  category: string | null;
  difficulty: Level | null;
  tags: string[];
  createdAt: string;
};

export type PublicDocumentDTO = {
  id: number;
  title: string;
  content: string;
  authorId: number;
  authorName: string;
  category: string | null;
  difficulty: Level | null;
  tags: string[];
  createdAt: string;
};

export type FindDocumentsParams = {
  category?: string;
  tag?: string;
  level?: Level;
  status?: Status;
};

export type PublicDocumentsParams = {
  category?: string;
  tag?: string;
  difficulty?: Level;
  page?: number;
  size?: number;
};

export async function sendDocument(request: DocumentRequestDTO): Promise<DocumentResponseDTO> {
  return apiRequest<DocumentResponseDTO>({ method: "POST", url: "/document", data: request });
}

export async function findDocumentById(documentId: number): Promise<DocumentResponseDTO> {
  return apiRequest<DocumentResponseDTO>({ method: "GET", url: `/document/${documentId}` });
}

export async function findPublicDocumentById(documentId: number): Promise<PublicDocumentDTO> {
  return apiRequest<PublicDocumentDTO>({ method: "GET", url: `/document/public/${documentId}` });
}

export async function listPublicDocuments(params: PublicDocumentsParams = {}): Promise<PublicDocumentDTO[]> {
  return apiRequest<PublicDocumentDTO[]>({ method: "GET", url: "/document/public", params });
}

export async function findDocuments(params: FindDocumentsParams = {}): Promise<DocumentListDTO[]> {
  return apiRequest<DocumentListDTO[]>({ method: "GET", url: "/document", params });
}

export async function updateDocumentVisibility(documentId: number, visibility: Visibility): Promise<DocumentResponseDTO> {
  return apiRequest<DocumentResponseDTO>({ method: "PATCH", url: `/document/${documentId}/visibility`, data: { visibility } });
}

export async function deleteDocument(documentId: number): Promise<void> {
  return apiRequest<void>({ method: "DELETE", url: `/document/${documentId}` });
}
