import { AlertTriangle, CheckCircle2, Clock, Eye, Loader2, Sparkles } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useI18n } from "@/lib/i18n";
import type { DocumentListDTO, Status } from "@/services/documents.service";

export function DocCard({ document, onOpen, onRequestSummary, summaryLoading = false }: {
  document: DocumentListDTO;
  onOpen: () => void;
  onRequestSummary?: () => void;
  summaryLoading?: boolean;
}) {
  const { lang, t } = useI18n();
  const statusMap: Record<Status, { label: string; className: string; Icon: typeof Clock }> = {
    PENDING: { label: t("library.status.pending"), className: "bg-muted text-muted-foreground", Icon: Clock },
    PROCESSING: { label: t("library.status.processing"), className: "bg-pessego text-marrom", Icon: Loader2 },
    PROCESSED: { label: t("library.status.processed"), className: "bg-verde text-verde-foreground", Icon: CheckCircle2 },
    ERROR: { label: t("library.status.error"), className: "bg-destructive text-destructive-foreground", Icon: AlertTriangle },
  };
  const levelMap = {
    BEGINNER: t("library.level.beginner"),
    INTERMEDIATE: t("library.level.intermediate"),
    ADVANCED: t("library.level.advanced"),
  } as const;
  const status = statusMap[document.status];
  const canSummarize = document.status === "PROCESSED" && !document.summary && Boolean(onRequestSummary);

  return (
    <article className="group flex min-h-80 flex-col rounded-2xl border border-border bg-card p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg">
      <div className="flex items-start justify-between gap-3">
        <Badge className={`gap-1.5 ${status.className}`}>
          <status.Icon className={`h-3.5 w-3.5 ${document.status === "PROCESSING" ? "animate-spin" : ""}`} aria-hidden="true" />
          {status.label}
        </Badge>
        <span className="text-xs text-muted-foreground">{new Date(document.createdAt).toLocaleDateString(lang)}</span>
      </div>

      <button type="button" onClick={onOpen} className="mt-4 rounded-lg text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" aria-label={`${t("library.details")}: ${document.title}`}>
        <h2 className="line-clamp-2 font-serif text-2xl leading-tight transition group-hover:text-vinho">{document.title}</h2>
        <p className="mt-2 text-xs font-medium uppercase tracking-wider text-muted-foreground">{document.visibility === "PUBLIC" ? t("common.public") : t("common.private")}</p>
      </button>

      <div className="mt-4 flex flex-wrap gap-2">
        {document.category && <span className="rounded-md bg-muted px-2 py-1 text-xs">{document.category}</span>}
        {document.difficulty && <span className="rounded-md border border-border px-2 py-1 text-xs text-muted-foreground">{levelMap[document.difficulty]}</span>}
      </div>

      {document.status === "PROCESSED" && (
        <section className="mt-4 rounded-xl border border-border/70 bg-muted/20 p-3.5" aria-label={t("library.summary")}>
          <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-muted-foreground">{t("library.summary")}</p>
          {document.summary ? (
            <p className="mt-2 line-clamp-4 text-sm leading-6 text-foreground/90">{document.summary}</p>
          ) : canSummarize ? (
            <Button type="button" size="sm" variant="outline" className="mt-3" onClick={onRequestSummary} disabled={summaryLoading}>
              {summaryLoading ? <Loader2 className="mr-1.5 h-3.5 w-3.5 animate-spin" aria-hidden="true" /> : <Sparkles className="mr-1.5 h-3.5 w-3.5" aria-hidden="true" />}
              {summaryLoading ? t("library.generatingSummary") : t("library.generateSummary")}
            </Button>
          ) : null}
        </section>
      )}

      {document.tags.length > 0 && <div className="mt-3 flex flex-wrap gap-1.5">{document.tags.slice(0, 5).map((tag) => <span key={tag} className="rounded-full bg-pessego/45 px-2 py-0.5 text-[11px] font-medium text-marrom">#{tag}</span>)}</div>}

      <div className="mt-4 border-t border-border pt-4">
        <Button type="button" size="sm" variant="outline" onClick={onOpen}>
          <Eye className="mr-1.5 h-3.5 w-3.5" aria-hidden="true" />
          {t("library.details")}
        </Button>
      </div>
    </article>
  );
}
