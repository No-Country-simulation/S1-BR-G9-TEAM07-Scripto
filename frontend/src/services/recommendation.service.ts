import { apiRequest } from "./api";
import type { Level } from "./classification.service";

export type RecommendationDTO = {
  documentId: number;
  title: string;
  authorName: string;
  category: string | null;
  difficulty: Level | null;
  tags: string[];
  summary: string | null;
  createdAt: string;
  score: number;
  semanticSimilarity: number;
};

export async function getExploreRecommendations(limit = 10): Promise<RecommendationDTO[]> {
  return apiRequest<RecommendationDTO[]>({
    method: "GET",
    url: "/explore/recommendations",
    params: { limit },
  });
}

export async function getDocumentRecommendations(
  documentId: number,
  limit = 10,
): Promise<RecommendationDTO[]> {
  return apiRequest<RecommendationDTO[]>({
    method: "GET",
    url: `/document/${documentId}/recommendations`,
    params: { limit },
  });
}
