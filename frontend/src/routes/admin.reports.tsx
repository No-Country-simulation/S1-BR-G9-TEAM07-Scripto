import { createFileRoute } from "@tanstack/react-router";
import { useCallback, useEffect, useState } from "react";
import { Ban, CheckCircle2, Flag, XCircle } from "lucide-react";
import { toast } from "sonner";
import { EmptyState, ErrorState, LoadingState } from "@/components/StatusState";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { reportReasonLabel } from "@/lib/display";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { listOpenReports, reviewReport, type ReportResponseDTO } from "@/services/report.service";

export const Route = createFileRoute("/admin/reports")({ component: AdminReports });

type ReviewAction = { report: ReportResponseDTO; status: "DISMISSED" | "ACTIONED"; blockDocument: boolean; label: string };

function AdminReports() {
  const { lang, t } = useI18n();
  const [reports, setReports] = useState<ReportResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState<ReviewAction | null>(null);
  const [reviewing, setReviewing] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setReports(await listOpenReports());
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => { void load(); }, [load]);

  async function confirmReview() {
    if (!pending) return;
    setReviewing(true); setError(null);
    try { await reviewReport(pending.report.id, { status: pending.status, blockDocument: pending.blockDocument }); toast.success(t("common.success")); setPending(null); await load(); }
    catch (currentError) { setError(friendlyError(currentError, t)); setPending(null); }
    finally { setReviewing(false); }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("admin.reports.eyebrow")} title={t("admin.reports.title")} description={t("admin.reports.subtitle")} />
      {error && <div className="mt-6"><ErrorState title={t("common.error")} description={error} onRetry={() => void load()} retryLabel={t("common.retry")} /></div>}
      <div className="mt-7">{loading ? <LoadingState label={t("common.loading")} /> : reports.length === 0 ? <EmptyState icon={Flag} title={t("admin.reports.empty.title")} description={t("admin.reports.empty.body")} /> : <div className="space-y-4">{reports.map((report) => <article key={report.id} className="rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-6"><div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between"><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><h2 className="font-serif text-2xl">#{report.id} · {lang === "pt-BR" ? "Documento" : "Document"} {report.documentId}</h2><Badge className="bg-dourado text-marrom">{reportReasonLabel(report.reason, t)}</Badge></div><p className="mt-2 text-sm text-muted-foreground">{lang === "pt-BR" ? "Enviada em" : "Submitted on"} {new Date(report.createdAt).toLocaleString(lang)}</p>{report.details && <p className="mt-4 max-w-3xl whitespace-pre-wrap rounded-xl bg-muted/35 p-4 text-sm leading-6">{report.details}</p>}</div><div className="flex shrink-0 flex-col gap-2 sm:flex-row xl:flex-col"><Button size="sm" variant="outline" onClick={() => setPending({ report, status: "DISMISSED", blockDocument: false, label: t("admin.reports.dismiss") })}><XCircle className="mr-2 h-4 w-4" />{t("admin.reports.dismiss")}</Button><Button size="sm" variant="outline" onClick={() => setPending({ report, status: "ACTIONED", blockDocument: false, label: t("admin.reports.action") })}><CheckCircle2 className="mr-2 h-4 w-4" />{t("admin.reports.action")}</Button><Button size="sm" className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={() => setPending({ report, status: "ACTIONED", blockDocument: true, label: t("admin.reports.block") })}><Ban className="mr-2 h-4 w-4" />{t("admin.reports.block")}</Button></div></div></article>)}</div>}</div>
      <Dialog open={Boolean(pending)} onOpenChange={(open) => !open && setPending(null)}><DialogContent><DialogHeader><DialogTitle>{t("admin.reports.confirmTitle")}</DialogTitle><DialogDescription>{t("admin.reports.confirmBody")}</DialogDescription></DialogHeader><div className="rounded-xl bg-muted/35 p-4 text-sm"><strong>{pending?.label}</strong><p className="mt-1 text-muted-foreground">{pending && `#${pending.report.id} · ${reportReasonLabel(pending.report.reason, t)}`}</p></div><DialogFooter><Button variant="outline" onClick={() => setPending(null)}>{t("common.cancel")}</Button><Button onClick={() => void confirmReview()} disabled={reviewing} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{reviewing ? t("common.loading") : t("common.confirm")}</Button></DialogFooter></DialogContent></Dialog>
    </div>
  );
}
