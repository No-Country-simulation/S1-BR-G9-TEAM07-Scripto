import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { Search, Shield, UserCog, Users } from "lucide-react";
import { EmptyState } from "@/components/StatusState";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { accountStatusLabel, roleLabel } from "@/lib/display";
import { useI18n } from "@/lib/i18n";
import { devAdminUsers, type AdminUserPreview } from "@/mocks/admin";

export const Route = createFileRoute("/admin/users")({ component: AdminUsers });

type RoleFilter = "" | AdminUserPreview["role"];
type StatusFilter = "" | AdminUserPreview["status"];

function AdminUsers() {
  const { lang, t } = useI18n();
  const [query, setQuery] = useState("");
  const [role, setRole] = useState<RoleFilter>("");
  const [status, setStatus] = useState<StatusFilter>("");
  const users = import.meta.env.DEV ? devAdminUsers : [];
  const filtered = useMemo(() => users.filter((user) => (!query || `${user.fullName} ${user.email}`.toLocaleLowerCase(lang).includes(query.toLocaleLowerCase(lang))) && (!role || user.role === role) && (!status || user.status === status)), [users, query, role, status, lang]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("admin.users.eyebrow")} title={t("admin.users.title")} description={t("admin.users.subtitle")} />
      <div className="mt-6 rounded-xl border border-dourado/35 bg-dourado/10 p-4 text-sm text-muted-foreground">{t("admin.users.pending")}{import.meta.env.DEV && <span className="mt-2 block font-medium text-foreground">DEV: {lang === "pt-BR" ? "dados demonstrativos isolados estão ativos." : "isolated demonstration data is enabled."}</span>}</div>
      <section className="mt-6 grid gap-3 rounded-2xl border border-border bg-card p-4 shadow-sm md:grid-cols-[1fr_180px_180px]">
        <div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" /><Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={t("admin.users.search")} className="pl-9" /></div>
        <select value={role} onChange={(event) => setRole(event.target.value as RoleFilter)} aria-label={t("admin.users.role")} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{t("admin.users.role")}</option><option value="USER">{roleLabel("USER", t)}</option><option value="ADMIN">{roleLabel("ADMIN", t)}</option></select>
        <select value={status} onChange={(event) => setStatus(event.target.value as StatusFilter)} aria-label={t("admin.users.status")} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{t("admin.users.status")}</option><option value="ACTIVE">{accountStatusLabel("ACTIVE", t)}</option><option value="SUSPENDED">{accountStatusLabel("SUSPENDED", t)}</option></select>
      </section>
      <div className="mt-7">
        {filtered.length === 0 ? <EmptyState icon={Users} title={import.meta.env.DEV ? (lang === "pt-BR" ? "Nenhum usuário demonstrativo encontrado" : "No demonstration users found") : t("common.backendPending")} description={import.meta.env.DEV ? undefined : t("admin.users.pending")} /> : (
          <div className="grid gap-4 lg:grid-cols-2">{filtered.map((user) => <article key={user.id} className="rounded-2xl border border-border bg-card p-5 shadow-sm"><div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><h2 className="truncate font-serif text-2xl">{user.fullName}</h2><Badge variant="outline">{roleLabel(user.role, t)}</Badge><Badge className={user.status === "ACTIVE" ? "bg-verde text-verde-foreground" : "bg-destructive text-destructive-foreground"}>{accountStatusLabel(user.status, t)}</Badge></div><p className="mt-1 break-all text-sm text-muted-foreground">{user.email}</p></div><Button size="sm" variant="outline" disabled title={t("common.backendPending")}><UserCog className="mr-2 h-4 w-4" />{t("common.edit")}</Button></div><dl className="mt-5 grid grid-cols-2 gap-3 rounded-xl bg-muted/30 p-4 text-sm"><Data icon={Shield} label={lang === "pt-BR" ? "Documentos" : "Documents"} value={user.documents} /><Data icon={Users} label={lang === "pt-BR" ? "Denúncias recebidas" : "Reports received"} value={user.reports} /></dl></article>)}</div>
        )}
      </div>
    </div>
  );
}
function Data({ icon: Icon, label, value }: { icon: typeof Shield; label: string; value: number }) { return <div><dt className="flex items-center gap-1.5 text-xs text-muted-foreground"><Icon className="h-3.5 w-3.5" />{label}</dt><dd className="mt-1 font-serif text-2xl">{value}</dd></div>; }
