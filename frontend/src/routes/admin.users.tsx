import { createFileRoute } from "@tanstack/react-router";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Ban, Search, Shield, UserCheck, UserCog, Users } from "lucide-react";
import { toast } from "sonner";
import { EmptyState, ErrorState, LoadingState } from "@/components/StatusState";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { listAdminUsers, setAdminUserBanned, updateAdminUser, type AdminAccountStatus, type AdminRole, type AdminUser } from "@/services/admin.service";

export const Route = createFileRoute("/admin/users")({ component: AdminUsers });

type RoleFilter = "" | AdminRole;
type StatusFilter = "" | AdminAccountStatus;

type EditState = { user: AdminUser; fullName: string; email: string; role: AdminRole; newPassword: string };

function statusLabel(status: AdminAccountStatus, lang: string) {
  if (status === "BANNED") return lang === "pt-BR" ? "Banido" : "Banned";
  if (status === "PENDING_DELETION") return lang === "pt-BR" ? "Exclusão pendente" : "Pending deletion";
  return lang === "pt-BR" ? "Ativo" : "Active";
}

function AdminUsers() {
  const { lang, t } = useI18n();
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [query, setQuery] = useState("");
  const [role, setRole] = useState<RoleFilter>("");
  const [status, setStatus] = useState<StatusFilter>("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [edit, setEdit] = useState<EditState | null>(null);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setUsers(await listAdminUsers()); }
    catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setLoading(false); }
  }, [t]);

  useEffect(() => { void load(); }, [load]);

  const filtered = useMemo(() => users.filter((user) =>
    (!query || `${user.fullName} ${user.email} ${user.cpf}`.toLocaleLowerCase(lang).includes(query.toLocaleLowerCase(lang)))
    && (!role || user.role === role)
    && (!status || user.accountStatus === status)
  ), [users, query, role, status, lang]);

  async function saveEdit() {
    if (!edit) return;
    setSaving(true); setError(null);
    try {
      const updated = await updateAdminUser(edit.user.id, {
        fullName: edit.fullName.trim(), email: edit.email.trim().toLowerCase(), role: edit.role,
        ...(edit.newPassword ? { newPassword: edit.newPassword } : {}),
      });
      setUsers((current) => current.map((item) => item.id === updated.id ? updated : item));
      setEdit(null); toast.success(t("common.success"));
    } catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setSaving(false); }
  }

  async function toggleBan(user: AdminUser) {
    const banning = user.accountStatus !== "BANNED";
    if (!window.confirm(banning
      ? (lang === "pt-BR" ? `Banir ${user.fullName}?` : `Ban ${user.fullName}?`)
      : (lang === "pt-BR" ? `Reativar o acesso de ${user.fullName}?` : `Restore access for ${user.fullName}?`))) return;
    try {
      const updated = await setAdminUserBanned(user.id, banning);
      setUsers((current) => current.map((item) => item.id === updated.id ? updated : item));
      toast.success(t("common.success"));
    } catch (currentError) { setError(friendlyError(currentError, t)); }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("admin.users.eyebrow")} title={t("admin.users.title")} description={t("admin.users.subtitle")} />
      {error && <div className="mt-6"><ErrorState title={t("common.error")} description={error} onRetry={() => void load()} retryLabel={t("common.retry")} /></div>}
      <section className="mt-6 grid gap-3 rounded-2xl border border-border bg-card p-4 shadow-sm md:grid-cols-[1fr_180px_200px]">
        <div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" /><Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={t("admin.users.search")} className="pl-9" /></div>
        <select value={role} onChange={(event) => setRole(event.target.value as RoleFilter)} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{t("admin.users.role")}</option><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select>
        <select value={status} onChange={(event) => setStatus(event.target.value as StatusFilter)} className="min-h-10 rounded-md border border-input bg-background px-3 text-sm"><option value="">{t("admin.users.status")}</option><option value="ACTIVE">{statusLabel("ACTIVE", lang)}</option><option value="PENDING_DELETION">{statusLabel("PENDING_DELETION", lang)}</option><option value="BANNED">{statusLabel("BANNED", lang)}</option></select>
      </section>
      <div className="mt-7">
        {loading ? <LoadingState label={t("common.loading")} /> : filtered.length === 0 ? <EmptyState icon={Users} title={lang === "pt-BR" ? "Nenhum usuário encontrado" : "No users found"} /> : (
          <div className="grid gap-4 lg:grid-cols-2">{filtered.map((user) => <article key={user.id} className="rounded-2xl border border-border bg-card p-5 shadow-sm"><div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><h2 className="truncate font-serif text-2xl">{user.fullName}</h2><Badge variant="outline">{user.role}</Badge><Badge className={user.accountStatus === "ACTIVE" ? "bg-verde text-verde-foreground" : "bg-destructive text-destructive-foreground"}>{statusLabel(user.accountStatus, lang)}</Badge></div><p className="mt-1 break-all text-sm text-muted-foreground">{user.email}</p><p className="mt-1 text-xs text-muted-foreground">CPF: {user.cpf}</p></div><div className="flex gap-2"><Button size="sm" variant="outline" onClick={() => setEdit({ user, fullName: user.fullName, email: user.email, role: user.role, newPassword: "" })}><UserCog className="mr-2 h-4 w-4" />{t("common.edit")}</Button><Button size="sm" variant="outline" onClick={() => void toggleBan(user)} className={user.accountStatus === "BANNED" ? "" : "text-destructive"}>{user.accountStatus === "BANNED" ? <UserCheck className="mr-2 h-4 w-4" /> : <Ban className="mr-2 h-4 w-4" />}{user.accountStatus === "BANNED" ? (lang === "pt-BR" ? "Desbanir" : "Unban") : (lang === "pt-BR" ? "Banir" : "Ban")}</Button></div></div><dl className="mt-5 grid grid-cols-3 gap-3 rounded-xl bg-muted/30 p-4 text-sm"><Data icon={Shield} label={lang === "pt-BR" ? "Documentos" : "Documents"} value={user.documentCount} /><Data icon={Users} label={lang === "pt-BR" ? "Denúncias feitas" : "Reports made"} value={user.reportsMade} /><Data icon={Users} label={lang === "pt-BR" ? "Denúncias recebidas" : "Reports received"} value={user.reportsReceived} /></dl></article>)}</div>
        )}
      </div>

      <Dialog open={Boolean(edit)} onOpenChange={(open) => !open && setEdit(null)}>
        <DialogContent><DialogHeader><DialogTitle>{lang === "pt-BR" ? "Editar usuário" : "Edit user"}</DialogTitle><DialogDescription>{edit?.user.email}</DialogDescription></DialogHeader>{edit && <div className="space-y-4"><div className="space-y-1.5"><Label htmlFor="admin-user-name">{lang === "pt-BR" ? "Nome completo" : "Full name"}</Label><Input id="admin-user-name" value={edit.fullName} onChange={(e) => setEdit({ ...edit, fullName: e.target.value })} /></div><div className="space-y-1.5"><Label htmlFor="admin-user-email">E-mail</Label><Input id="admin-user-email" type="email" value={edit.email} onChange={(e) => setEdit({ ...edit, email: e.target.value })} /></div><div className="space-y-1.5"><Label htmlFor="admin-user-role">Role</Label><select id="admin-user-role" value={edit.role} onChange={(e) => setEdit({ ...edit, role: e.target.value as AdminRole })} className="min-h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></div><div className="space-y-1.5"><Label htmlFor="admin-user-password">{lang === "pt-BR" ? "Nova senha (opcional)" : "New password (optional)"}</Label><Input id="admin-user-password" type="password" autoComplete="new-password" value={edit.newPassword} onChange={(e) => setEdit({ ...edit, newPassword: e.target.value })} /></div></div>}<DialogFooter><Button variant="outline" onClick={() => setEdit(null)}>{t("common.cancel")}</Button><Button onClick={() => void saveEdit()} disabled={saving || !edit?.fullName || !edit?.email} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{saving ? t("common.loading") : t("common.confirm")}</Button></DialogFooter></DialogContent>
      </Dialog>
    </div>
  );
}
function Data({ icon: Icon, label, value }: { icon: typeof Shield; label: string; value: number }) { return <div><dt className="flex items-center gap-1.5 text-xs text-muted-foreground"><Icon className="h-3.5 w-3.5" />{label}</dt><dd className="mt-1 font-serif text-2xl">{value}</dd></div>; }
