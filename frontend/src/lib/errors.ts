import type { TranslationKey } from "./i18n";
import { ApiError } from "@/services/api";

type Translate = (key: TranslationKey, fallback?: string) => string;

function isSafeMessage(message: string): boolean {
  const normalized = message.trim();
  return (
    normalized.length > 0 &&
    normalized.length <= 180 &&
    !/[{}\[\]]/.test(normalized) &&
    !normalized.includes("\n") &&
    !/\b(stack|trace|exception|axios|request config)\b/i.test(normalized)
  );
}

export function friendlyError(error: unknown, t: Translate): string {
  if (error instanceof ApiError) {
    if (error.status === 0) return t("error.network");
    const translated = t(`error.${error.status}`, "");
    return translated || t("common.error");
  }

  if (error instanceof Error && isSafeMessage(error.message)) return error.message.trim();
  return t("common.error");
}
