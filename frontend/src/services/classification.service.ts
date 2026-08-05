export type Level = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type ClassificationSource = "LOCAL" | "NEMOTRON";
export type FallbackReason =
  | "LOCAL_MODEL_DISABLED"
  | "LOCAL_MODEL_ERROR"
  | "INVALID_EMBEDDING"
  | "UNSUPPORTED_CATEGORY"
  | "OTHER_REQUIRES_FALLBACK"
  | "LOW_CATEGORY_CONFIDENCE"
  | "OUT_OF_DISTRIBUTION"
  | "LOW_DIFFICULTY_CONFIDENCE"
  | "NO_VALID_TAGS"
  | "INVALID_LOCAL_OUTPUT"
  | "EXTERNAL_AI_NOT_ALLOWED";

export type AIAnalysisResultDTO = {
  analysisId: number;
  category: string;
  categoryConfidence: number | null;
  tags: string[];
  difficulty: Level;
  difficultyConfidence: number | null;
  source: ClassificationSource;
  modelVersion: string | null;
  externalModel: string | null;
  fallbackReasons: FallbackReason[];
  suggestedCategory: string | null;
  createdAt: string;
};
