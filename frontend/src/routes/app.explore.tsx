import { createFileRoute } from "@tanstack/react-router";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Flag, RefreshCw, Sparkles } from "lucide-react";
import { toast } from "sonner";
import { EmptyState, ErrorState, LoadingState } from "@/components/StatusState";
import { PageHeader } from "@/components/PageHeader";
import { Pagination } from "@/components/Pagination";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { levelLabel, reportReasonLabel } from "@/lib/display";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { findPublicDocumentById, listPublicDocuments, type PublicDocumentDTO } from "@/services/documents.service";
import { getExploreRecommendations, type RecommendationDTO } from "@/services/recommendation.service";
import { reportDocument, type ReportReason } from "@/services/report.service";

export const Route = createFileRoute("/app/explore")({ component: ExplorePage });

const PAGE_SIZE = 15;
const reasons: ReportReason[] = ["SPAM", "HARASSMENT", "HATEFUL_CONTENT", "ILLEGAL_CONTENT", "COPYRIGHT", "MISINFORMATION", "OTHER"];
type ReportTarget = { documentId: number; title: string };

function ExplorePage() {
  const { lang, t } = useI18n();
  const [recommended, setRecommended] = useState<RecommendationDTO[]>([]);
  const [publicItems, setPublicItems] = useState<PublicDocumentDTO[]>([]);
  const [category, setCategory] = useState("");
  const [tag, setTag] = useState("");
  const [page, setPage] = useState(1);
  const [detail, setDetail] = useState<PublicDocumentDTO | null>(null);
  const [reportTarget, setReportTarget] = useState<ReportTarget | null>(null);
  const [reason, setReason] = useState<ReportReason>("SPAM");
  const [details, setDetails] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reporting, setReporting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      const [recommendations, catalog] = await Promise.all([
        getExploreRecommendations(3),
        listPublicDocuments({ category: category || undefined, tag: tag || undefined, page: page - 1, size: PAGE_SIZE }),
      ]);
      setRecommended(recommendations);
      setPublicItems(catalog);
    } catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setLoading(false); }
  }, [category, tag, page, t]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => { setPage(1); }, [category, tag]);

  const filterItems = useMemo(() => [...recommended.map((item) => ({ category: item.category, tags: item.tags })), ...publicItems.map((item) => ({ category: item.category, tags: item.tags }))], [recommended, publicItems]);
  const categories = useMemo(() => [...new Set(filterItems.map((item) => item.category).filter((value): value is string => Boolean(value)))].sort(), [filterItems]);
  const tags = useMemo(() => [...new Set(filterItems.flatMap((item) => item.tags))].sort(), [filterItems]);
  const pageCount = publicItems.length === PAGE_SIZE ? page + 1 : page;

  async function openDocument(documentId: number) {
    setError(null);
    try { setDetail(await findPublicDocumentById(documentId)); }
    catch (currentError) { setError(friendlyError(currentError, t)); }
  }

  async function submitReport() {
    if (!reportTarget) return;
    setReporting(true); setError(null);
    try {
      await reportDocument(reportTarget.documentId, { reason, details: details.trim() || undefined });
      toast.success(t("explore.report.success")); setReportTarget(null); setDetails(""); setReason("SPAM");
    } catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setReporting(false); }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("explore.eyebrow")} title={t("explore.title")} description={t("explore.subtitle")} actions={<Button variant="outline" onClick={() => void load()} disabled={loading}><RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />{t("common.refresh")}</Button>} />
      <section className="mt-7 grid gap-3 rounded-2xl border border-border bg-card p-4 shadow-sm sm:grid-cols-2 sm:p-5" aria-label={t("common.filters")}><select value={category} onChange={(event) => setCategory(event.target.value)} aria-label={t("library.allCategories")} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{t("library.allCategories")}</option>{categories.map((value) => <option key={value} value={value}>{value}</option>)}</select><select value={tag} onChange={(event) => setTag(event.target.value)} aria-label={t("library.allTags")} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{t("library.allTags")}</option>{tags.map((value) => <option key={value} value={value}>{value}</option>)}</select></section>
      {error && <div className="mt-6"><ErrorState title={t("common.error")} description={error} onRetry={() => void load()} retryLabel={t("common.retry")} /></div>}
      {loading ? <div className="mt-7"><LoadingState label={t("common.loading")} /></div> : recommended.length === 0 && publicItems.length === 0 ? <div className="mt-7"><EmptyState title={t("explore.empty.title")} description={t("explore.empty.body")} /></div> : <>
        {recommended.length > 0 && <section className="mt-9"><div className="flex items-center gap-2"><Sparkles className="h-5 w-5 text-dourado" /><h2 className="font-serif text-2xl">{t("explore.recommended")}</h2></div><div className="mt-4 grid gap-5 lg:grid-cols-3">{recommended.map((item) => <RecommendationCard key={item.documentId} item={item} onOpen={() => void openDocument(item.documentId)} onReport={() => setReportTarget({ documentId: item.documentId, title: item.title })} />)}</div></section>}
        <section className="mt-10"><h2 className="font-serif text-2xl">{t("explore.public")}</h2>{publicItems.length === 0 ? <div className="mt-4"><EmptyState title={t("explore.empty.title")} description={t("explore.empty.body")} /></div> : <><div className="mt-4 grid gap-5 sm:grid-cols-2 xl:grid-cols-3">{publicItems.map((item) => <PublicCard key={item.id} item={item} onOpen={() => void openDocument(item.id)} onReport={() => setReportTarget({ documentId: item.id, title: item.title })} />)}</div><Pagination page={page} pageCount={pageCount} onPageChange={setPage} /></>}</section>
      </>}

      <Dialog open={Boolean(detail)} onOpenChange={(open) => !open && setDetail(null)}><DialogContent className="max-h-[88vh] max-w-3xl overflow-y-auto"><DialogHeader><DialogTitle>{detail?.title}</DialogTitle><DialogDescription>{detail && `${t("explore.author")}: ${detail.authorName} · ${new Date(detail.createdAt).toLocaleDateString(lang)}`}</DialogDescription></DialogHeader>{detail && <div className="space-y-5"><div className="flex flex-wrap gap-2">{detail.category && <span className="rounded-md bg-muted px-2 py-1 text-xs">{detail.category}</span>}{detail.difficulty && <span className="rounded-md border border-border px-2 py-1 text-xs text-muted-foreground">{levelLabel(detail.difficulty, t)}</span>}{detail.tags.map((value) => <span key={value} className="rounded-full bg-pessego/50 px-2 py-1 text-xs text-marrom">#{value}</span>)}</div><div className="whitespace-pre-wrap break-words rounded-xl border border-border bg-muted/20 p-4 text-sm leading-7">{detail.content}</div></div>}</DialogContent></Dialog>
      <Dialog open={Boolean(reportTarget)} onOpenChange={(open) => !open && setReportTarget(null)}><DialogContent><DialogHeader><DialogTitle>{t("explore.report.title")}</DialogTitle><DialogDescription>{reportTarget?.title}</DialogDescription></DialogHeader><div className="space-y-4"><div className="space-y-1.5"><Label htmlFor="report-reason">{t("explore.report.reason")}</Label><select id="report-reason" value={reason} onChange={(event) => setReason(event.target.value as ReportReason)} className="min-h-10 w-full rounded-md border border-input bg-background px-3 text-sm">{reasons.map((value) => <option key={value} value={value}>{reportReasonLabel(value, t)}</option>)}</select></div><div className="space-y-1.5"><Label htmlFor="report-details">{t("explore.report.details")} ({t("common.optional")})</Label><Textarea id="report-details" rows={5} maxLength={500} value={details} onChange={(event) => setDetails(event.target.value)} placeholder={t("explore.report.detailsPlaceholder")} /><p className="text-right text-xs text-muted-foreground">{details.length}/500</p></div></div><DialogFooter><Button variant="outline" onClick={() => setReportTarget(null)}>{t("common.cancel")}</Button><Button onClick={() => void submitReport()} disabled={reporting} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{reporting ? t("common.loading") : t("explore.report.submit")}</Button></DialogFooter></DialogContent></Dialog>
    </div>
  );
}

function RecommendationCard({ item, onOpen, onReport }: { item: RecommendationDTO; onOpen: () => void; onReport: () => void }) {
  const { lang, t } = useI18n();
  return <article className="flex min-h-64 flex-col rounded-2xl border border-dourado/60 bg-card p-5 shadow-sm"><span className="mb-3 inline-flex w-fit items-center gap-1 rounded-full bg-dourado/15 px-2.5 py-1 text-[11px] font-semibold"><Sparkles className="h-3 w-3 text-dourado" />{t("explore.recommended")}</span><button type="button" onClick={onOpen} className="text-left"><h3 className="line-clamp-2 font-serif text-2xl">{item.title}</h3><p className="mt-3 text-sm text-muted-foreground">{item.category ?? "—"}</p></button><TagList tags={item.tags} /><div className="mt-auto flex items-center justify-between gap-3 border-t border-border pt-4 text-xs text-muted-foreground"><span>{lang === "pt-BR" ? "Afinidade" : "Affinity"}: {Math.round(item.score * 100)}%</span><ReportButton onClick={onReport} /></div></article>;
}
function PublicCard({ item, onOpen, onReport }: { item: PublicDocumentDTO; onOpen: () => void; onReport: () => void }) {
  const { t } = useI18n();
  return <article className="flex min-h-64 flex-col rounded-2xl border border-border bg-card p-5 shadow-sm"><button type="button" onClick={onOpen} className="text-left"><h3 className="line-clamp-2 font-serif text-2xl">{item.title}</h3><p className="mt-3 text-sm text-muted-foreground">{item.authorName} · {item.category ?? "—"}</p></button><TagList tags={item.tags} /><div className="mt-auto flex items-center justify-end border-t border-border pt-4 text-xs text-muted-foreground"><ReportButton onClick={onReport} /></div></article>;
}
function TagList({ tags }: { tags: string[] }) { return <div className="mt-3 flex flex-wrap gap-1.5">{tags.slice(0, 5).map((value) => <span key={value} className="rounded-full bg-pessego/50 px-2 py-0.5 text-xs text-marrom">#{value}</span>)}</div>; }
function ReportButton({ onClick }: { onClick: () => void }) { const { t } = useI18n(); return <button type="button" onClick={onClick} className="inline-flex items-center gap-1 rounded px-1 py-1 text-vinho hover:underline"><Flag className="h-3.5 w-3.5" />{t("explore.report")}</button>; }
