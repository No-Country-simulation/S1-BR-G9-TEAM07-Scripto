import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  BarChart3,
  Check,
  CheckCircle2,
  FileText,
  Globe2,
  Lock,
  Mail,
  Palette,
  Pencil,
  Save,
  ShieldCheck,
  Sun,
  Moon,
  Tag,
  Trash2,
  TrendingUp,
  UserRound,
} from "lucide-react";
import { toast } from "sonner";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PasswordField } from "@/components/PasswordField";
import { PasswordRequirements } from "@/components/PasswordRequirements";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { levelLabel, roleLabel } from "@/lib/display";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { useTheme } from "@/lib/theme";
import { isStrongPassword, isValidEmail, maskCpf } from "@/lib/validation";
import { currentSession, deleteOwnAccount } from "@/services/auth.service";
import { findDocuments, type DocumentListDTO } from "@/services/documents.service";
import { updateStoredSessionUser } from "@/services/session";
import { changeOwnPassword, getOwnStatistics, updateOwnProfile, type UserProfileStatsDTO } from "@/services/user.service";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/app/profile")({ component: ProfilePage });

function ProfilePage() {
  const { lang, setLang, t } = useI18n();
  const { theme, setTheme } = useTheme();
  const router = useRouter();
  const [session, setSession] = useState(currentSession());
  const [stats, setStats] = useState<UserProfileStatsDTO | null>(null);
  const [documents, setDocuments] = useState<DocumentListDTO[]>([]);
  const [profile, setProfile] = useState({ fullName: session?.user.fullName ?? "", email: session?.user.email ?? "" });
  const [editingProfile, setEditingProfile] = useState(false);
  const [passwords, setPasswords] = useState({ current: "", next: "", confirm: "" });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    Promise.all([getOwnStatistics(), findDocuments()])
      .then(([statistics, library]) => {
        if (!active) return;
        setStats(statistics);
        setDocuments(library);
      })
      .catch((currentError) => {
        if (active) setError(friendlyError(currentError, t));
      });
    return () => {
      active = false;
    };
  }, [t]);

  const metrics = useMemo(
    () => ({
      total: documents.length,
      published: documents.filter((document) => document.visibility === "PUBLIC").length,
      tags: new Set(documents.flatMap((document) => document.tags)).size,
    }),
    [documents],
  );

  const profileValid = profile.fullName.trim().split(/\s+/).length >= 2 && isValidEmail(profile.email);
  const passwordValid = Boolean(passwords.current) && isStrongPassword(passwords.next) && passwords.next === passwords.confirm;

  if (!session) return null;

  const initials = session.user.fullName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");

  async function saveProfile(event: React.FormEvent) {
    event.preventDefault();
    if (!profileValid) return;
    setSavingProfile(true);
    setError(null);
    try {
      const updated = await updateOwnProfile({
        fullName: profile.fullName.trim(),
        email: profile.email.trim().toLowerCase(),
      });
      const stored = updateStoredSessionUser(updated);
      setSession(stored);
      setProfile({ fullName: updated.fullName, email: updated.email });
      setEditingProfile(false);
      toast.success(t("profile.edit.success"));
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    } finally {
      setSavingProfile(false);
    }
  }

  function cancelProfileEdit() {
    setProfile({ fullName: session?.user.fullName ?? "", email: session?.user.email ?? "" });
    setEditingProfile(false);
  }

  async function savePassword(event: React.FormEvent) {
    event.preventDefault();
    if (!passwordValid) return;
    setSavingPassword(true);
    setError(null);
    try {
      await changeOwnPassword({
        currentPassword: passwords.current,
        newPassword: passwords.next,
        confirmNewPassword: passwords.confirm,
      });
      setPasswords({ current: "", next: "", confirm: "" });
      toast.success(t("profile.security.success"));
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    } finally {
      setSavingPassword(false);
    }
  }

  async function removeAccount() {
    setDeleting(true);
    setError(null);
    try {
      await deleteOwnAccount();
      await router.navigate({ to: "/" });
    } catch (currentError) {
      setError(friendlyError(currentError, t));
      setConfirmDelete(false);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="mx-auto w-full px-4 py-7 sm:px-6 sm:py-9 lg:w-[75vw] lg:max-w-[90rem]">
      <header className="mb-8 flex items-center gap-3">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-vinho/10">
          <UserRound className="h-[18px] w-[18px] text-vinho" aria-hidden="true" />
        </div>
        <div>
          <h1 className="font-serif text-3xl font-semibold tracking-tight text-foreground">{t("profile.title")}</h1>
          <p className="mt-0.5 text-sm text-muted-foreground">{t("profile.subtitle")}</p>
        </div>
      </header>

      <div className="mb-8 flex items-center gap-4" aria-hidden="true">
        <span className="h-px flex-1 bg-gradient-to-r from-transparent via-dourado/50 to-dourado/20" />
        <span className="font-serif text-lg text-dourado">✦</span>
        <span className="h-px flex-1 bg-gradient-to-l from-transparent via-dourado/50 to-dourado/20" />
      </div>

      {error && (
        <p role="alert" className="mb-6 rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">
          {error}
        </p>
      )}

      <div className="space-y-6">
        <section aria-labelledby="profile-stats-heading" className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
          <div className="flex items-center gap-3 border-b border-border px-5 py-4 sm:px-6">
            <BarChart3 className="h-[17px] w-[17px] text-vinho" aria-hidden="true" />
            <h2 id="profile-stats-heading" className="text-base font-semibold text-foreground">
              {t("profile.stats.title")}
            </h2>
          </div>
          <div className="p-5 sm:p-6">
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              <FeaturedStat icon={FileText} label={t("profile.stats.total")} value={metrics.total} />
              <FeaturedStat icon={CheckCircle2} label={t("profile.stats.processed")} value={stats?.totalProcessedDocuments ?? 0} tone="success" />
              <CompactStat icon={Globe2} label={t("profile.stats.published")} value={metrics.published} />
              <CompactStat icon={BarChart3} label={t("profile.stats.category")} value={stats?.mostFrequentCategory ?? "—"} tone="gold" />
              <CompactStat
                icon={TrendingUp}
                label={t("profile.stats.level")}
                value={stats?.mostFrequentLevel ? levelLabel(stats.mostFrequentLevel, t) : "—"}
                tone="success"
              />
              <CompactStat icon={Tag} label={t("profile.stats.tags")} value={metrics.tags} tone="muted" />
            </div>
          </div>
        </section>

        <section aria-labelledby="profile-info-heading" className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
          <div className="flex items-center justify-between border-b border-border px-5 py-4 sm:px-6">
            <div className="flex items-center gap-3">
              <UserRound className="h-[17px] w-[17px] text-vinho" aria-hidden="true" />
              <h2 id="profile-info-heading" className="text-base font-semibold text-foreground">
                {t("profile.personal.title")}
              </h2>
            </div>
            {!editingProfile && (
              <button
                type="button"
                onClick={() => setEditingProfile(true)}
                className="flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium text-muted-foreground transition hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
                {t("profile.edit")}
              </button>
            )}
          </div>

          <div className="p-5 sm:p-6">
            <div className="mb-6 flex items-center gap-4">
              <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-vinho text-vinho-foreground shadow-sm">
                <span className="font-serif text-xl font-bold">{initials}</span>
              </div>
              <div className="min-w-0">
                <p className="truncate font-semibold text-foreground">{session.user.fullName}</p>
                <p className="truncate text-sm text-muted-foreground">{session.user.email}</p>
              </div>
            </div>

            <form onSubmit={saveProfile} className="space-y-4">
              {editingProfile ? (
                <>
                  <FormField
                    id="profile-fullName"
                    label={t("auth.fullName")}
                    error={profile.fullName.trim().split(/\s+/).length < 2 ? t("auth.validation.name") : undefined}
                    required
                  >
                    <Input
                      id="profile-fullName"
                      autoComplete="name"
                      value={profile.fullName}
                      onChange={(event) => setProfile((current) => ({ ...current, fullName: event.target.value }))}
                    />
                  </FormField>
                  <FormField id="profile-email" label={t("auth.email")} error={!isValidEmail(profile.email) ? t("auth.validation.email") : undefined} required>
                    <Input
                      id="profile-email"
                      type="email"
                      autoComplete="email"
                      value={profile.email}
                      onChange={(event) => setProfile((current) => ({ ...current, email: event.target.value }))}
                    />
                  </FormField>
                </>
              ) : (
                <>
                  <ReadOnlyProfileField icon={UserRound} label={t("auth.fullName")} value={session.user.fullName} />
                  <ReadOnlyProfileField icon={Mail} label={t("auth.email")} value={session.user.email} />
                </>
              )}

              <div>
                <div className="mb-1.5 flex items-center gap-2">
                  <span className="text-sm font-medium text-foreground">{t("auth.cpf")}</span>
                  <span className="rounded bg-muted px-1.5 py-0.5 text-[10px] text-muted-foreground">{t("profile.readOnly")}</span>
                </div>
                <div className="flex items-center gap-2.5 rounded-lg border border-border/60 bg-muted/30 px-3.5 py-2.5">
                  <span className="font-mono text-sm tracking-wider text-muted-foreground">{maskCpf(session.user.cpf ?? "") || "—"}</span>
                </div>
              </div>

              <ReadOnlyProfileField icon={ShieldCheck} label={t("admin.users.role")} value={roleLabel(session.access, t)} subtle />

              {editingProfile && (
                <div className="flex items-center justify-end gap-3 border-t border-border pt-5">
                  <Button type="button" variant="ghost" onClick={cancelProfileEdit} disabled={savingProfile}>
                    {t("common.cancel")}
                  </Button>
                  <LoadingButton
                    type="submit"
                    loading={savingProfile}
                    disabled={!profileValid}
                    className="bg-vinho text-vinho-foreground hover:bg-vinho/90"
                  >
                    <Save className="mr-2 h-4 w-4" aria-hidden="true" />
                    {t("common.save")}
                  </LoadingButton>
                </div>
              )}
            </form>
          </div>
        </section>

        <section aria-labelledby="profile-password-heading" className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
          <div className="flex items-center gap-3 border-b border-border px-5 py-4 sm:px-6">
            <Lock className="h-[17px] w-[17px] text-vinho" aria-hidden="true" />
            <div>
              <h2 id="profile-password-heading" className="text-base font-semibold text-foreground">
                {t("profile.security.title")}
              </h2>
              <p className="mt-0.5 text-xs text-muted-foreground">{t("profile.security.subtitle")}</p>
            </div>
          </div>
          <form onSubmit={savePassword} className="space-y-4 p-5 sm:p-6">
            <FormField id="current-password" label={t("auth.currentPassword")} required>
              <PasswordField
                id="current-password"
                autoComplete="current-password"
                value={passwords.current}
                onChange={(event) => setPasswords((current) => ({ ...current, current: event.target.value }))}
              />
            </FormField>
            <FormField
              id="new-password"
              label={t("auth.newPassword")}
              error={passwords.next && !isStrongPassword(passwords.next) ? t("auth.validation.password") : undefined}
              required
            >
              <PasswordField
                id="new-password"
                autoComplete="new-password"
                value={passwords.next}
                onChange={(event) => setPasswords((current) => ({ ...current, next: event.target.value }))}
              />
            </FormField>
            <PasswordRequirements password={passwords.next} />
            <FormField
              id="confirm-new-password"
              label={t("auth.confirmPassword")}
              error={passwords.confirm && passwords.confirm !== passwords.next ? t("auth.validation.passwordMatch") : undefined}
              required
            >
              <PasswordField
                id="confirm-new-password"
                autoComplete="new-password"
                value={passwords.confirm}
                onChange={(event) => setPasswords((current) => ({ ...current, confirm: event.target.value }))}
              />
            </FormField>
            <div className="flex justify-end pt-1">
              <LoadingButton
                type="submit"
                loading={savingPassword}
                disabled={!passwordValid}
                className="bg-vinho text-vinho-foreground hover:bg-vinho/90"
              >
                {t("profile.security.submit")}
              </LoadingButton>
            </div>
          </form>
        </section>

        <section aria-labelledby="profile-preferences-heading" className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
          <div className="flex items-center gap-3 border-b border-border px-5 py-4 sm:px-6">
            <Palette className="h-[17px] w-[17px] text-vinho" aria-hidden="true" />
            <h2 id="profile-preferences-heading" className="text-base font-semibold text-foreground">
              {t("profile.preferences.title")}
            </h2>
          </div>
          <div className="space-y-6 p-5 sm:p-6">
            <div>
              <p className="mb-3 text-sm font-medium text-foreground">{t("profile.preferences.theme")}</p>
              <div className="flex flex-wrap gap-3" role="radiogroup" aria-label={t("profile.preferences.theme")}>
                <PreferenceButton active={theme === "light"} onClick={() => setTheme("light")} icon={Sun} label={t("profile.preferences.light")} />
                <PreferenceButton active={theme === "dark"} onClick={() => setTheme("dark")} icon={Moon} label={t("profile.preferences.dark")} />
              </div>
            </div>
            <div>
              <p className="mb-3 text-sm font-medium text-foreground">{t("profile.preferences.language")}</p>
              <div className="flex flex-col gap-3 sm:flex-row" role="radiogroup" aria-label={t("profile.preferences.language")}>
                <PreferenceButton active={lang === "pt-BR"} onClick={() => setLang("pt-BR")} label="🇧🇷 Português (Brasil)" />
                <PreferenceButton active={lang === "en"} onClick={() => setLang("en")} label="🇺🇸 English" />
              </div>
            </div>
          </div>
        </section>

        <section aria-labelledby="profile-terms-heading" className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
          <div className="flex items-center gap-3 border-b border-border px-5 py-4 sm:px-6">
            <FileText className="h-[17px] w-[17px] text-vinho" aria-hidden="true" />
            <h2 id="profile-terms-heading" className="text-base font-semibold text-foreground">
              {t("profile.terms.title")}
            </h2>
          </div>
          <div className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6">
            <p className="max-w-md text-sm leading-relaxed text-muted-foreground">{t("profile.terms.body")}</p>
            <Link to="/terms" className="self-start sm:self-center">
              <Button variant="outline">{t("profile.terms.action")}</Button>
            </Link>
          </div>
        </section>

        <section aria-labelledby="profile-delete-heading" className="overflow-hidden rounded-xl border border-destructive/30 bg-card shadow-sm">
          <div className="flex items-center gap-3 border-b border-destructive/20 px-5 py-4 sm:px-6">
            <Trash2 className="h-[17px] w-[17px] text-destructive" aria-hidden="true" />
            <h2 id="profile-delete-heading" className="text-base font-semibold text-foreground">
              {t("profile.delete.title")}
            </h2>
          </div>
          <div className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6">
            <div>
              <p className="text-sm font-medium text-foreground">{t("profile.delete.action")}</p>
              <p className="mt-1 max-w-md text-sm leading-relaxed text-muted-foreground">{t("profile.delete.body")}</p>
            </div>
            <Button
              variant="outline"
              className="self-start border-destructive/40 text-destructive hover:bg-destructive/10 sm:self-center"
              onClick={() => setConfirmDelete(true)}
            >
              <Trash2 className="mr-2 h-4 w-4" aria-hidden="true" />
              {t("profile.delete.action")}
            </Button>
          </div>
        </section>
      </div>

      <Dialog open={confirmDelete} onOpenChange={setConfirmDelete}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("profile.delete.confirmTitle")}</DialogTitle>
            <DialogDescription>{t("profile.delete.confirmBody")}</DialogDescription>
          </DialogHeader>
          <div className="flex items-start gap-3 rounded-xl border border-destructive/20 bg-destructive/5 p-4 text-sm text-muted-foreground">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-destructive" aria-hidden="true" />
            <span>{t("profile.delete.body")}</span>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmDelete(false)}>
              {t("common.cancel")}
            </Button>
            <LoadingButton
              loading={deleting}
              onClick={() => void removeAccount()}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {t("profile.delete.confirmAction")}
            </LoadingButton>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

type IconType = typeof UserRound;
type Tone = "brand" | "success" | "gold" | "muted";

function toneClasses(tone: Tone) {
  if (tone === "success") return { icon: "text-verde", bg: "bg-verde/10" };
  if (tone === "gold") return { icon: "text-dourado", bg: "bg-dourado/10" };
  if (tone === "muted") return { icon: "text-muted-foreground", bg: "bg-muted" };
  return { icon: "text-vinho", bg: "bg-vinho/10" };
}

function FeaturedStat({ icon: Icon, label, value, tone = "brand" }: { icon: IconType; label: string; value: string | number; tone?: Tone }) {
  const classes = toneClasses(tone);
  return (
    <div className="col-span-1 flex items-center gap-4 rounded-xl border border-border/60 bg-muted/50 p-4 sm:col-span-2">
      <div className={cn("flex h-11 w-11 shrink-0 items-center justify-center rounded-xl", classes.bg)}>
        <Icon className={cn("h-5 w-5", classes.icon)} aria-hidden="true" />
      </div>
      <div className="min-w-0">
        <p className="break-words font-serif text-2xl font-bold text-foreground">{value}</p>
        <p className="mt-0.5 text-xs text-muted-foreground">{label}</p>
      </div>
    </div>
  );
}

function CompactStat({ icon: Icon, label, value, tone = "brand" }: { icon: IconType; label: string; value: string | number; tone?: Tone }) {
  const classes = toneClasses(tone);
  return (
    <div className="col-span-1 flex min-h-32 flex-col gap-2 rounded-xl border border-border/60 bg-muted/30 p-3.5">
      <div className={cn("flex h-8 w-8 items-center justify-center rounded-lg", classes.bg)}>
        <Icon className={cn("h-[15px] w-[15px]", classes.icon)} aria-hidden="true" />
      </div>
      <p className="break-words text-sm font-bold leading-tight text-foreground sm:text-base">{value}</p>
      <p className="text-[11px] leading-tight text-muted-foreground">{label}</p>
    </div>
  );
}

function ReadOnlyProfileField({ icon: Icon, label, value, subtle = false }: { icon: IconType; label: string; value: string; subtle?: boolean }) {
  return (
    <div>
      <p className="mb-1.5 text-sm font-medium text-foreground">{label}</p>
      <div className={cn("flex items-center gap-2.5 rounded-lg border px-3.5 py-2.5", subtle ? "border-border/40 bg-muted/30" : "border-border/60 bg-muted/50")}>
        <Icon className="h-[15px] w-[15px] shrink-0 text-muted-foreground" aria-hidden="true" />
        <span className="break-all text-sm text-foreground">{value}</span>
      </div>
    </div>
  );
}

function PreferenceButton({ active, onClick, label, icon: Icon }: { active: boolean; onClick: () => void; label: string; icon?: IconType }) {
  return (
    <button
      type="button"
      role="radio"
      aria-checked={active}
      onClick={onClick}
      className={cn(
        "flex items-center gap-2.5 rounded-lg border px-4 py-2.5 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
        active ? "border-vinho/40 bg-vinho/10 text-vinho" : "border-border bg-muted/40 text-muted-foreground hover:bg-muted hover:text-foreground",
      )}
    >
      {Icon && <Icon className="h-[15px] w-[15px]" aria-hidden="true" />}
      <span>{label}</span>
      {active && <Check className="ml-1 h-3.5 w-3.5" aria-hidden="true" />}
    </button>
  );
}
