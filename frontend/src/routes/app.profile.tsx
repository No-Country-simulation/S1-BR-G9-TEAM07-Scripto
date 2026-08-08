import { createFileRoute, useRouter } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { AlertTriangle, BarChart3, Mail, Save, ShieldCheck, UserRound } from "lucide-react";
import { toast } from "sonner";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PageHeader } from "@/components/PageHeader";
import { PasswordField } from "@/components/PasswordField";
import { PasswordRequirements } from "@/components/PasswordRequirements";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { levelLabel, roleLabel } from "@/lib/display";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { isStrongPassword, isValidEmail, maskCpf } from "@/lib/validation";
import { currentSession, deleteOwnAccount } from "@/services/auth.service";
import { updateStoredSessionUser } from "@/services/session";
import { changeOwnPassword, getOwnStatistics, updateOwnProfile, type UserProfileStatsDTO } from "@/services/user.service";

export const Route = createFileRoute("/app/profile")({ component: ProfilePage });

function ProfilePage() {
  const { t } = useI18n();
  const router = useRouter();
  const [session, setSession] = useState(currentSession());
  const [stats, setStats] = useState<UserProfileStatsDTO | null>(null);
  const [profile, setProfile] = useState({ fullName: session?.user.fullName ?? "", email: session?.user.email ?? "" });
  const [passwords, setPasswords] = useState({ current: "", next: "", confirm: "" });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    getOwnStatistics()
      .then((result) => { if (active) setStats(result); })
      .catch((currentError) => { if (active) setError(friendlyError(currentError, t)); });
    return () => { active = false; };
  }, [t]);

  const profileValid = profile.fullName.trim().split(/\s+/).length >= 2 && isValidEmail(profile.email);
  const passwordValid = Boolean(passwords.current) && isStrongPassword(passwords.next) && passwords.next === passwords.confirm;

  if (!session) return null;
  const initials = session.user.fullName.split(" ").filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join("");

  async function saveProfile(event: React.FormEvent) {
    event.preventDefault(); if (!profileValid) return;
    setSavingProfile(true); setError(null);
    try {
      const updated = await updateOwnProfile({ fullName: profile.fullName.trim(), email: profile.email.trim().toLowerCase() });
      const stored = updateStoredSessionUser(updated);
      setSession(stored);
      setProfile({ fullName: updated.fullName, email: updated.email });
      toast.success(t("profile.edit.success"));
    } catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setSavingProfile(false); }
  }

  async function savePassword(event: React.FormEvent) {
    event.preventDefault(); if (!passwordValid) return;
    setSavingPassword(true); setError(null);
    try {
      await changeOwnPassword({ currentPassword: passwords.current, newPassword: passwords.next, confirmNewPassword: passwords.confirm });
      setPasswords({ current: "", next: "", confirm: "" });
      toast.success(t("profile.security.success"));
    } catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setSavingPassword(false); }
  }

  async function removeAccount() {
    setDeleting(true); setError(null);
    try { await deleteOwnAccount(); await router.navigate({ to: "/" }); }
    catch (currentError) { setError(friendlyError(currentError, t)); setConfirmDelete(false); }
    finally { setDeleting(false); }
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("profile.eyebrow")} title={t("profile.title")} description={t("profile.subtitle")} />
      {error && <p role="alert" className="mt-6 rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">{error}</p>}

      <section className="mt-8 grid gap-6 rounded-2xl border border-border bg-card p-5 shadow-sm md:grid-cols-[auto_1fr] sm:p-7">
        <div className="flex h-24 w-24 items-center justify-center rounded-full bg-vinho font-serif text-3xl text-vinho-foreground" aria-label={session.user.fullName}>{initials}</div>
        <form onSubmit={saveProfile} className="space-y-5">
          <div><h2 className="font-serif text-2xl">{t("profile.personal.title")}</h2><p className="mt-1 text-sm text-muted-foreground">{t("profile.personal.subtitle")}</p></div>
          <div className="grid gap-4 sm:grid-cols-2">
            <FormField id="profile-fullName" label={t("auth.fullName")} error={profile.fullName.trim().split(/\s+/).length < 2 ? t("auth.validation.name") : undefined} required><Input id="profile-fullName" autoComplete="name" value={profile.fullName} onChange={(event) => setProfile((current) => ({ ...current, fullName: event.target.value }))} /></FormField>
            <FormField id="profile-email" label={t("auth.email")} error={!isValidEmail(profile.email) ? t("auth.validation.email") : undefined} required><Input id="profile-email" type="email" autoComplete="email" value={profile.email} onChange={(event) => setProfile((current) => ({ ...current, email: event.target.value }))} /></FormField>
            <FormField id="profile-cpf" label={t("auth.cpf")}><Input id="profile-cpf" value={maskCpf(session.user.cpf ?? "")} readOnly className="bg-muted/40" /></FormField>
            <FormField id="profile-access" label={t("admin.users.role")}><Input id="profile-access" value={roleLabel(session.access, t)} readOnly className="bg-muted/40" /></FormField>
          </div>
          <LoadingButton type="submit" loading={savingProfile} disabled={!profileValid} className="bg-vinho text-vinho-foreground hover:bg-vinho/90"><Save className="mr-2 h-4 w-4" />{t("common.save")}</LoadingButton>
        </form>
      </section>

      <div className="mt-8 grid gap-8 lg:grid-cols-[1.1fr_0.9fr]">
        <section className="rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7">
          <div className="flex items-start gap-3"><ShieldCheck className="mt-1 h-5 w-5 text-dourado" /><div><h2 className="font-serif text-2xl">{t("profile.security.title")}</h2><p className="mt-1 text-sm text-muted-foreground">{t("profile.security.subtitle")}</p></div></div>
          <form onSubmit={savePassword} className="mt-6 space-y-4">
            <FormField id="current-password" label={t("auth.currentPassword")} required><PasswordField id="current-password" autoComplete="current-password" value={passwords.current} onChange={(event) => setPasswords((current) => ({ ...current, current: event.target.value }))} /></FormField>
            <FormField id="new-password" label={t("auth.newPassword")} error={passwords.next && !isStrongPassword(passwords.next) ? t("auth.validation.password") : undefined} required><PasswordField id="new-password" autoComplete="new-password" value={passwords.next} onChange={(event) => setPasswords((current) => ({ ...current, next: event.target.value }))} /></FormField>
            <PasswordRequirements password={passwords.next} />
            <FormField id="confirm-new-password" label={t("auth.confirmPassword")} error={passwords.confirm && passwords.confirm !== passwords.next ? t("auth.validation.passwordMatch") : undefined} required><PasswordField id="confirm-new-password" autoComplete="new-password" value={passwords.confirm} onChange={(event) => setPasswords((current) => ({ ...current, confirm: event.target.value }))} /></FormField>
            <LoadingButton type="submit" loading={savingPassword} disabled={!passwordValid} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("profile.security.submit")}</LoadingButton>
          </form>
        </section>

        <section>
          <div className="flex items-center gap-2"><BarChart3 className="h-5 w-5 text-dourado" /><h2 className="font-serif text-2xl">{t("profile.stats.title")}</h2></div>
          <div className="mt-4 grid gap-4 sm:grid-cols-3"><Stat icon={UserRound} label={t("profile.stats.total")} value={stats?.totalProcessedDocuments ?? 0} /><Stat icon={Mail} label={t("profile.stats.category")} value={stats?.mostFrequentCategory ?? "—"} /><Stat icon={BarChart3} label={t("profile.stats.level")} value={stats?.mostFrequentLevel ? levelLabel(stats.mostFrequentLevel, t) : "—"} /></div>
        </section>
      </div>

      <section className="mt-10 rounded-2xl border border-destructive/30 bg-destructive/5 p-5 sm:p-7">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"><div className="flex items-start gap-3"><AlertTriangle className="mt-1 h-5 w-5 text-destructive" /><div><h2 className="font-serif text-2xl text-destructive">{t("profile.delete.title")}</h2><p className="mt-1 max-w-2xl text-sm text-muted-foreground">{t("profile.delete.body")}</p></div></div><Button variant="outline" className="border-destructive text-destructive hover:bg-destructive/10" onClick={() => setConfirmDelete(true)}>{t("profile.delete.action")}</Button></div>
      </section>

      <Dialog open={confirmDelete} onOpenChange={setConfirmDelete}><DialogContent><DialogHeader><DialogTitle>{t("profile.delete.confirmTitle")}</DialogTitle><DialogDescription>{t("profile.delete.confirmBody")}</DialogDescription></DialogHeader><DialogFooter><Button variant="outline" onClick={() => setConfirmDelete(false)}>{t("common.cancel")}</Button><LoadingButton loading={deleting} onClick={() => void removeAccount()} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">{t("profile.delete.confirmAction")}</LoadingButton></DialogFooter></DialogContent></Dialog>
    </div>
  );
}

function Stat({ icon: Icon, label, value }: { icon: typeof UserRound; label: string; value: string | number }) { return <div className="rounded-2xl border border-border bg-card p-5 shadow-sm"><Icon className="h-4 w-4 text-dourado" /><p className="mt-4 text-xs font-semibold uppercase tracking-wider text-muted-foreground">{label}</p><p className="mt-1 break-words font-serif text-3xl">{value}</p></div>; }
