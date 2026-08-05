import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { Eye, FileText, Search, Trash2 } from "lucide-react";
import { EmptyState } from "@/components/StatusState";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useI18n } from "@/lib/i18n";
import { devAdminDocuments } from "@/mocks/admin";

export const Route = createFileRoute("/admin/documents")({ component: AdminDocuments });

function AdminDocuments() {
  const { lang, t } = useI18n();
  const [query, setQuery] = useState("");
  const [visibility, setVisibility] = useState("");
  const documents = import.meta.env.DEV ? devAdminDocuments : [];
  const filtered = useMemo(() => documents.filter((document) => (!query || `${document.title} ${document.author}`.toLocaleLowerCase(lang).includes(query.toLocaleLowerCase(lang))) && (!visibility || document.visibility === visibility)), [documents, query, visibility, lang]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("admin.documents.eyebrow")} title={t("admin.documents.title")} description={t("admin.documents.subtitle")} />
      <div className="mt-6 rounded-xl border border-dourado/35 bg-dourado/10 p-4 text-sm text-muted-foreground">{t("admin.documents.pending")}{import.meta.env.DEV && <span className="mt-2 block font-medium text-foreground">DEV: {lang === "pt-BR" ? "dados demonstrativos isolados estão ativos." : "isolated demonstration data is enabled."}</span>}</div>
      <section className="mt-6 grid gap-3 rounded-2xl border border-border bg-card p-4 shadow-sm md:grid-cols-[1fr_200px]">
        <div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" /><Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={lang === "pt-BR" ? "Buscar por título ou autor…" : "Search by title or author…"} className="pl-9" /></div>
        <select value={visibility} onChange={(event) => setVisibility(event.target.value)} aria-label={lang === "pt-BR" ? "Visibilidade" : "Visibility"} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{lang === "pt-BR" ? "Todas as visibilidades" : "All visibility settings"}</option><option value="PUBLIC">{t("common.public")}</option><option value="PRIVATE">{t("common.private")}</option></select>
      </section>
      <div className="mt-7">{filtered.length === 0 ? <EmptyState icon={FileText} title={import.meta.env.DEV ? (lang === "pt-BR" ? "Nenhum documento demonstrativo encontrado" : "No demonstration documents found") : t("common.backendPending")} description={import.meta.env.DEV ? undefined : t("admin.documents.pending")} /> : <div className="grid gap-4 lg:grid-cols-2">{filtered.map((document) => <article key={document.id} className="rounded-2xl border border-border bg-card p-5 shadow-sm"><div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"><div><div className="flex flex-wrap items-center gap-2"><h2 className="font-serif text-2xl">{document.title}</h2><Badge variant="outline">{document.visibility === "PUBLIC" ? t("common.public") : t("common.private")}</Badge></div><p className="mt-1 text-sm text-muted-foreground">{document.author}</p></div><Badge className={document.reports > 0 ? "bg-destructive text-destructive-foreground" : "bg-muted text-muted-foreground"}>{document.reports} {lang === "pt-BR" ? "denúncia(s)" : "report(s)"}</Badge></div><div className="mt-5 flex flex-wrap gap-2 border-t border-border pt-4"><Button size="sm" variant="outline" disabled title={t("common.backendPending")}><Eye className="mr-2 h-4 w-4" />{lang === "pt-BR" ? "Visualizar" : "View"}</Button><Button size="sm" variant="outline" disabled title={t("common.backendPending")}>{lang === "pt-BR" ? "Alterar visibilidade" : "Change visibility"}</Button><Button size="sm" variant="ghost" disabled title={t("common.backendPending")} className="text-destructive"><Trash2 className="mr-2 h-4 w-4" />{t("common.delete")}</Button></div></article>)}</div>}</div>
    </div>
  );
}
