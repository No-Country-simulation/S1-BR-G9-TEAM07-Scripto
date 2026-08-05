import { apiRequest } from "./api";

export type RecommendationDTO = {
  documentId: number;
  title: string;
  category: string | null;
  tags: string[];
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
