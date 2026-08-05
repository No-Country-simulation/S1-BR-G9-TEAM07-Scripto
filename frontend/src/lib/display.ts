import type { TranslationKey } from "@/lib/i18n";
import type { Level } from "@/services/classification.service";
import type { ModerationStatus, Status } from "@/services/documents.service";
import type { ReportReason } from "@/services/report.service";

type Translate = (key: TranslationKey, fallback?: string) => string;

export function documentStatusLabel(status: Status, t: Translate): string {
  const keys: Record<Status, string> = {
    PENDING: "library.status.pending",
    PROCESSING: "library.status.processing",
    PROCESSED: "library.status.processed",
    ERROR: "library.status.error",
  };
  return t(keys[status]);
}

export function levelLabel(level: Level, t: Translate): string {
  const keys: Record<Level, string> = {
    BEGINNER: "library.level.beginner",
    INTERMEDIATE: "library.level.intermediate",
    ADVANCED: "library.level.advanced",
  };
  return t(keys[level]);
}

export function reportReasonLabel(reason: ReportReason, t: Translate): string {
  return t(`enum.reason.${reason}`);
}

export function roleLabel(role: "USER" | "ADMIN", t: Translate): string {
  return t(`enum.role.${role}`);
}

export function accountStatusLabel(status: "ACTIVE" | "SUSPENDED", t: Translate): string {
  return t(`enum.account.${status}`);
}

export function moderationStatusLabel(status: ModerationStatus, t: Translate): string {
  return t(`enum.moderation.${status}`);
}
