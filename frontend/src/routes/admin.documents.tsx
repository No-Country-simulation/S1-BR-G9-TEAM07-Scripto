import { createFileRoute } from "@tanstack/react-router";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Ban, Eye, EyeOff, FileText, Search, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { EmptyState, ErrorState, LoadingState } from "@/components/StatusState";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { getAdminDocument, listAdminDocuments, updateAdminDocument, type AdminDocument, type AdminDocumentDetail, type DocumentVisibility } from "@/services/admin.service";

export const Route = createFileRoute("/admin/documents")({ component: AdminDocuments });

function AdminDocuments() {
  const { lang, t } = useI18n();
  const [documents, setDocuments] = useState<AdminDocument[]>([]);
  const [query, setQuery] = useState("");
  const [visibility, setVisibility] = useState<"" | DocumentVisibility>("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [detail, setDetail] = useState<AdminDocumentDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setDocuments(await listAdminDocuments()); }
    catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setLoading(false); }
  }, [t]);
  useEffect(() => { void load(); }, [load]);

  const filtered = useMemo(() => documents.filter((document) =>
    (!query || `${document.title} ${document.ownerName}`.toLocaleLowerCase(lang).includes(query.toLocaleLowerCase(lang)))
    && (!visibility || document.visibility === visibility)
  ), [documents, query, visibility, lang]);

  async function openDetail(document: AdminDocument) {
    setDetailLoading(true); setError(null);
    try { setDetail(await getAdminDocument(document.id)); }
    catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setDetailLoading(false); }
  }

  async function patch(document: AdminDocument, request: Parameters<typeof updateAdminDocument>[1]) {
    try {
      const updated = await updateAdminDocument(document.id, request);
      setDocuments((current) => current.map((item) => item.id === updated.id ? updated : item));
      if (detail?.id === updated.id) setDetail(await getAdminDocument(updated.id));
      toast.success(t("common.success"));
    } catch (currentError) { setError(friendlyError(currentError, t)); }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("admin.documents.eyebrow")} title={t("admin.documents.title")} description={t("admin.documents.subtitle")} />
      {error && <div className="mt-6"><ErrorState title={t("common.error")} description={error} onRetry={() => void load()} retryLabel={t("common.retry")} /></div>}
      <section className="mt-6 grid gap-3 rounded-2xl border border-border bg-card p-4 shadow-sm md:grid-cols-[1fr_200px]">
        <div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" /><Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={lang === "pt-BR" ? "Buscar por título ou autor…" : "Search by title or author…"} className="pl-9" /></div>
        <select value={visibility} onChange={(event) => setVisibility(event.target.value as "" | DocumentVisibility)} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{lang === "pt-BR" ? "Todas as visibilidades" : "All visibility settings"}</option><option value="PUBLIC">{t("common.public")}</option><option value="PRIVATE">{t("common.private")}</option></select>
      </section>
      <div className="mt-7">{loading ? <LoadingState label={t("common.loading")} /> : filtered.length === 0 ? <EmptyState icon={FileText} title={lang === "pt-BR" ? "Nenhum documento encontrado" : "No documents found"} /> : <div className="grid gap-4 lg:grid-cols-2">{filtered.map((document) => <article key={document.id} className="rounded-2xl border border-border bg-card p-5 shadow-sm"><div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"><div><div className="flex flex-wrap items-center gap-2"><h2 className="font-serif text-2xl">{document.title}</h2><Badge variant="outline">{document.visibility === "PUBLIC" ? t("common.public") : t("common.private")}</Badge><Badge className={document.moderationStatus === "BLOCKED" ? "bg-destructive text-destructive-foreground" : "bg-muted text-muted-foreground"}>{document.moderationStatus}</Badge></div><p className="mt-1 text-sm text-muted-foreground">{document.ownerName} · {document.status}</p></div><Badge className={document.reportCount > 0 ? "bg-destructive text-destructive-foreground" : "bg-muted text-muted-foreground"}>{document.reportCount} {lang === "pt-BR" ? "denúncia(s)" : "report(s)"}</Badge></div><div className="mt-5 flex flex-wrap gap-2 border-t border-border pt-4"><Button size="sm" variant="outline" onClick={() => void openDetail(document)} disabled={detailLoading}><Eye className="mr-2 h-4 w-4" />{lang === "pt-BR" ? "Visualizar" : "View"}</Button><Button size="sm" variant="outline" onClick={() => void patch(document, { visibility: document.visibility === "PUBLIC" ? "PRIVATE" : "PUBLIC" })}>{document.visibility === "PUBLIC" ? <EyeOff className="mr-2 h-4 w-4" /> : <Eye className="mr-2 h-4 w-4" />}{lang === "pt-BR" ? "Alternar visibilidade" : "Toggle visibility"}</Button><Button size="sm" variant="outline" onClick={() => void patch(document, { blocked: document.moderationStatus !== "BLOCKED" })} className={document.moderationStatus === "BLOCKED" ? "" : "text-destructive"}>{document.moderationStatus === "BLOCKED" ? <ShieldCheck className="mr-2 h-4 w-4" /> : <Ban className="mr-2 h-4 w-4" />}{document.moderationStatus === "BLOCKED" ? (lang === "pt-BR" ? "Desbloquear" : "Unblock") : (lang === "pt-BR" ? "Bloquear" : "Block")}</Button></div></article>)}</div>}</div>

      <Dialog open={Boolean(detail)} onOpenChange={(open) => !open && setDetail(null)}>
        <DialogContent className="max-h-[88vh] max-w-3xl overflow-y-auto"><DialogHeader><DialogTitle>{detail?.title}</DialogTitle><DialogDescription>{detail && `${detail.ownerName} · ${detail.status} · ${detail.visibility} · ${detail.moderationStatus}`}</DialogDescription></DialogHeader>{detail && <div className="space-y-4"><div className="whitespace-pre-wrap break-words rounded-xl border border-border bg-muted/20 p-4 text-sm leading-7">{detail.content}</div><p className="text-xs text-muted-foreground">{detail.reportCount} {lang === "pt-BR" ? "denúncia(s)" : "report(s)"}</p></div>}</DialogContent>
      </Dialog>
    </div>
  );
}
