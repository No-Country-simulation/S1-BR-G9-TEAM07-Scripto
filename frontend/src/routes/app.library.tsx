import { createFileRoute, Link } from "@tanstack/react-router";
import { useCallback, useEffect, useMemo, useState } from "react";
import { AlertTriangle, Eye, Filter, RefreshCw, Search, Sparkles, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { DocCard } from "@/components/common/DocCard";
import { EmptyState, ErrorState, LoadingState } from "@/components/StatusState";
import { PageHeader } from "@/components/PageHeader";
import { Pagination } from "@/components/Pagination";
import { LoadingButton } from "@/components/LoadingButton";
import { Button } from "@/components/ui/button";
import { AlertDialog, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { documentStatusLabel, levelLabel, moderationStatusLabel } from "@/lib/display";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import type { Level } from "@/services/classification.service";
import { deleteDocument, findDocumentById, findDocuments, updateDocumentVisibility, type DocumentListDTO, type DocumentResponseDTO, type Status } from "@/services/documents.service";
import { summarizeDocument } from "@/services/summary.service";

export const Route = createFileRoute("/app/library")({ component: LibraryPage });

const PAGE_SIZE = 9;
type Sort = "newest" | "oldest" | "title";

function LibraryPage() {
  const { lang, t } = useI18n();
  const [documents, setDocuments] = useState<DocumentListDTO[]>([]);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("");
  const [tag, setTag] = useState("");
  const [level, setLevel] = useState<Level | "">("");
  const [status, setStatus] = useState<Status | "">("");
  const [sort, setSort] = useState<Sort>("newest");
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<DocumentResponseDTO | null>(null);
  const [summaryLoadingId, setSummaryLoadingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setDocuments(await findDocuments());
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => { setPage(1); }, [query, category, tag, level, status, sort]);

  const categories = useMemo(() => [...new Set(documents.map((document) => document.category).filter((value): value is string => Boolean(value)))].sort(), [documents]);
  const tags = useMemo(() => [...new Set(documents.flatMap((document) => document.tags))].sort(), [documents]);
  const filtered = useMemo(() => {
    const normalized = query.trim().toLocaleLowerCase(lang);
    return documents
      .filter((document) => {
        const haystack = [document.title, document.category ?? "", ...document.tags].join(" ").toLocaleLowerCase(lang);
        return (!normalized || haystack.includes(normalized)) && (!category || document.category === category) && (!tag || document.tags.includes(tag)) && (!level || document.difficulty === level) && (!status || document.status === status);
      })
      .sort((left, right) => {
        if (sort === "title") return left.title.localeCompare(right.title, lang);
        const delta = new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime();
        return sort === "oldest" ? delta : -delta;
      });
  }, [documents, query, category, tag, level, status, sort, lang]);
  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const visible = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  async function openDocument(documentId: number) {
    setError(null);
    try { setSelected(await findDocumentById(documentId)); }
    catch (currentError) { setError(friendlyError(currentError, t)); }
  }

  async function requestSummary(documentId: number) {
    setSummaryLoadingId(documentId);
    setError(null);
    try {
      const generated = await summarizeDocument(documentId);
      setDocuments((current) => current.map((document) => document.documentId === documentId ? { ...document, summary: generated.summary } : document));
      setSelected((current) => current?.documentId === documentId ? { ...current, summary: generated.summary } : current);
      toast.success(t("library.summaryReady"));
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    } finally {
      setSummaryLoadingId(null);
    }
  }


  async function changeVisibility() {
    if (!selected) return;
    setError(null);
    try {
      const next = selected.visibility === "PUBLIC" ? "PRIVATE" : "PUBLIC";
      const updated = await updateDocumentVisibility(selected.documentId, next);
      setSelected(updated);
      await load();
      toast.success(lang === "pt-BR" ? "Visibilidade atualizada." : "Visibility updated.");
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    }
  }

  async function removeSelected() {
    if (!selected) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteDocument(selected.documentId);
      setDeleteOpen(false);
      setSelected(null);
      await load();
      toast.success(lang === "pt-BR" ? "Documento excluído." : "Document deleted.");
    } catch (currentError) {
      const message = friendlyError(currentError, t);
      setDeleteError(message);
      setError(message);
    } finally {
      setDeleting(false);
    }
  }

  const filtersActive = Boolean(query || category || tag || level || status);
  function clearFilters() { setQuery(""); setCategory(""); setTag(""); setLevel(""); setStatus(""); setSort("newest"); }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader
        eyebrow={t("library.eyebrow")}
        title={t("library.title")}
        description={t("library.subtitle")}
        actions={<><Button type="button" variant="outline" onClick={() => void load()} disabled={loading}><RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} aria-hidden="true" />{t("common.refresh")}</Button><Link to="/app"><Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("common.newDocument")}</Button></Link></>}
      />

      <section className="mt-7 rounded-2xl border border-border bg-card p-4 shadow-sm sm:p-5" aria-label={t("common.filters")}>
        <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-[minmax(220px,1.4fr)_repeat(5,minmax(130px,1fr))]">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
            <Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={t("library.search.placeholder")} className="pl-9" aria-label={t("common.search")} />
          </div>
          <Select value={category} onChange={setCategory} label={t("library.allCategories")} options={categories} />
          <Select value={tag} onChange={setTag} label={t("library.allTags")} options={tags} />
          <select value={level} onChange={(event) => setLevel(event.target.value as Level | "")} aria-label={t("library.allLevels")} className="min-h-10 rounded-md border border-input bg-field px-3 dark:bg-background text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
            <option value="">{t("library.allLevels")}</option><option value="BEGINNER">{t("library.level.beginner")}</option><option value="INTERMEDIATE">{t("library.level.intermediate")}</option><option value="ADVANCED">{t("library.level.advanced")}</option>
          </select>
          <select value={status} onChange={(event) => setStatus(event.target.value as Status | "")} aria-label={t("library.allStatuses")} className="min-h-10 rounded-md border border-input bg-field px-3 dark:bg-background text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
            <option value="">{t("library.allStatuses")}</option><option value="PENDING">{t("library.status.pending")}</option><option value="PROCESSING">{t("library.status.processing")}</option><option value="PROCESSED">{t("library.status.processed")}</option>
          </select>
          <select value={sort} onChange={(event) => setSort(event.target.value as Sort)} aria-label={lang === "pt-BR" ? "Ordenar" : "Sort"} className="min-h-10 rounded-md border border-input bg-field px-3 dark:bg-background text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
            <option value="newest">{t("library.sort.newest")}</option><option value="oldest">{t("library.sort.oldest")}</option><option value="title">{t("library.sort.title")}</option>
          </select>
        </div>
        {filtersActive && <Button type="button" variant="ghost" size="sm" className="mt-3" onClick={clearFilters}><Filter className="mr-1.5 h-4 w-4" aria-hidden="true" />{t("common.clearFilters")}</Button>}
      </section>


      {error && <div className="mt-6"><ErrorState title={t("common.error")} description={error} onRetry={() => void load()} retryLabel={t("common.retry")} /></div>}
      <div className="mt-7">
        {loading ? <LoadingState label={t("common.loading")} /> : visible.length === 0 ? (
          <EmptyState title={t("library.empty.title")} description={t("library.empty.body")} action={filtersActive ? <Button variant="outline" onClick={clearFilters}>{t("common.clearFilters")}</Button> : <Link to="/app"><Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("common.newDocument")}</Button></Link>} />
        ) : (
          <><div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">{visible.map((document) => <DocCard key={document.documentId} document={document} onOpen={() => void openDocument(document.documentId)} onRequestSummary={document.status === "PROCESSED" && !document.summary ? () => void requestSummary(document.documentId) : undefined} summaryLoading={summaryLoadingId === document.documentId} />)}</div><Pagination page={page} pageCount={pageCount} onPageChange={setPage} /></>
        )}
      </div>

      <Dialog open={Boolean(selected)} onOpenChange={(open) => !open && setSelected(null)}>
        <DialogContent className="max-h-[88vh] max-w-3xl overflow-y-auto">
          <DialogHeader><DialogTitle>{selected?.title}</DialogTitle><DialogDescription>{selected && `${selected.visibility === "PUBLIC" ? t("common.public") : t("common.private")} · ${new Date(selected.createdAt).toLocaleString(lang)}`}</DialogDescription></DialogHeader>
          {selected && <div className="space-y-6">
            <dl className="grid gap-4 rounded-xl bg-muted/30 p-4 text-sm sm:grid-cols-2"><Data label="Status" value={documentStatusLabel(selected.status, t)} /><Data label={lang === "pt-BR" ? "Moderação" : "Moderation"} value={moderationStatusLabel(selected.moderationStatus, t)} /></dl>
            {selected.analysis && <section><h3 className="font-serif text-xl">{t("library.analysis")}</h3><div className="mt-3 flex flex-wrap gap-2"><span className="rounded-md bg-muted px-2 py-1 text-xs">{selected.analysis.category}</span><span className="rounded-md border border-border px-2 py-1 text-xs text-muted-foreground">{levelLabel(selected.analysis.difficulty, t)}</span>{selected.analysis.tags.map((value) => <span key={value} className="rounded-full bg-pessego/50 px-2 py-1 text-xs text-marrom">#{value}</span>)}</div></section>}
            {selected.status === "PROCESSED" && <section><h3 className="font-serif text-xl">{t("library.summary")}</h3><div className="mt-3 rounded-xl border border-border bg-muted/20 p-4">{selected.summary ? <p className="whitespace-pre-wrap text-sm leading-7">{selected.summary}</p> : <Button onClick={() => void requestSummary(selected.documentId)} disabled={summaryLoadingId === selected.documentId} className="bg-vinho text-vinho-foreground hover:bg-vinho/90"><Sparkles className="mr-2 h-4 w-4" />{summaryLoadingId === selected.documentId ? t("library.generatingSummary") : t("library.generateSummary")}</Button>}</div></section>}
            <section><h3 className="font-serif text-xl">{t("library.content")}</h3><div className="mt-3 whitespace-pre-wrap break-words rounded-xl border border-border bg-muted/20 p-4 text-sm leading-7">{selected.content}</div></section>
            <div className="flex flex-wrap gap-2 border-t border-border pt-4"><Button variant="outline" onClick={() => void changeVisibility()}><Eye className="mr-2 h-4 w-4" />{t("library.changeVisibility")}</Button><Button variant="outline" onClick={() => { setDeleteError(null); setDeleteOpen(true); }} className="text-destructive"><Trash2 className="mr-2 h-4 w-4" />{t("library.deleteDocument")}</Button></div>
          </div>}
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteOpen} onOpenChange={(open) => { if (!open && !deleting) setDeleteOpen(false); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-destructive/10 sm:mx-0"><AlertTriangle className="h-6 w-6 text-destructive" aria-hidden="true" /></div>
            <AlertDialogTitle>{t("library.deleteConfirm.title")}</AlertDialogTitle>
            <AlertDialogDescription>{t("library.deleteConfirm.body")}</AlertDialogDescription>
            {selected && <p className="break-words rounded-lg border border-border bg-muted/40 px-3 py-2 text-sm font-medium text-foreground">“{selected.title}”</p>}
            {deleteError && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{deleteError}</p>}
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleting}>{t("common.cancel")}</AlertDialogCancel>
            <LoadingButton loading={deleting} loadingLabel={t("library.deleting")} onClick={() => void removeSelected()} disabled={deleting} className="bg-destructive text-destructive-foreground hover:bg-destructive/90"><Trash2 className="mr-2 h-4 w-4" />{t("library.deleteConfirm.confirm")}</LoadingButton>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </div>
  );
}

function Select({ value, onChange, label, options }: { value: string; onChange: (value: string) => void; label: string; options: string[] }) {
  return <select value={value} onChange={(event) => onChange(event.target.value)} aria-label={label} className="min-h-10 rounded-md border border-input bg-field px-3 dark:bg-background text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"><option value="">{label}</option>{options.map((option) => <option key={option} value={option}>{option}</option>)}</select>;
}
function Data({ label, value }: { label: string; value: React.ReactNode }) { return <div><dt className="text-xs font-medium uppercase tracking-wider text-muted-foreground">{label}</dt><dd className="mt-1 font-medium">{value}</dd></div>; }
