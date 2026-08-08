import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { AlertTriangle, CheckCircle2, KeyRound } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { CpfInput } from "@/components/CpfInput";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PasswordField } from "@/components/PasswordField";
import { PasswordRequirements } from "@/components/PasswordRequirements";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { isCpfShapeValid, isStrongPassword, isValidEmail } from "@/lib/validation";
import { changeSuspendedPassword, reactivateAccount } from "@/services/auth.service";

export const Route = createFileRoute("/account-suspended")({ component: ReactivationPage });

function ReactivationPage() {
  const { lang, t } = useI18n();
  const router = useRouter();
  const [cpf, setCpf] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [touched, setTouched] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [passwordDialog, setPasswordDialog] = useState(false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmNewPassword, setConfirmNewPassword] = useState("");
  const [changingPassword, setChangingPassword] = useState(false);

  const errors = useMemo(() => ({ cpf: isCpfShapeValid(cpf) ? "" : t("auth.validation.cpf"), email: isValidEmail(email) ? "" : t("auth.validation.email"), password: password ? "" : t("auth.validation.required") }), [cpf, email, password, t]);
  const valid = !errors.cpf && !errors.email && !errors.password;
  const passwordChangeValid = isCpfShapeValid(cpf) && isValidEmail(email) && Boolean(currentPassword) && isStrongPassword(newPassword) && newPassword === confirmNewPassword;

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setTouched(true);
    if (!valid) return;
    setError(null); setLoading(true);
    try {
      await reactivateAccount({ cpf: cpf.replace(/\D/g, ""), email: email.trim().toLowerCase(), password });
      setSuccess(true);
    } catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setLoading(false); }
  }

  async function submitPasswordChange() {
    if (!passwordChangeValid) return;
    setChangingPassword(true); setError(null);
    try {
      await changeSuspendedPassword({ cpf, email, currentPassword, newPassword, confirmNewPassword });
      setPasswordDialog(false); setCurrentPassword(""); setNewPassword(""); setConfirmNewPassword("");
      setPassword(newPassword);
    } catch (currentError) { setError(friendlyError(currentError, t)); }
    finally { setChangingPassword(false); }
  }

  return (
    <AuthShell title={t("auth.reactivate.title")} subtitle={t("auth.reactivate.subtitle")}>
      <div className="mb-5 flex items-start gap-3 rounded-lg border border-dourado/40 bg-dourado/10 p-4 text-sm"><AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-dourado" aria-hidden="true" /><p className="leading-relaxed text-muted-foreground">{t("auth.reactivate.notice")}</p></div>
      {success ? (
        <div className="space-y-4 text-center"><CheckCircle2 className="mx-auto h-10 w-10 text-verde" aria-hidden="true" /><p className="text-sm">{t("auth.reactivate.success")}</p><Button className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90" onClick={() => void router.navigate({ to: "/login" })}>{t("auth.reactivate.goLogin")}</Button></div>
      ) : (
        <form onSubmit={submit} className="space-y-4" noValidate>
          <FormField id="reactivate-cpf" label={t("auth.cpf")} error={touched ? errors.cpf : undefined} required><CpfInput id="reactivate-cpf" value={cpf} onValueChange={setCpf} aria-invalid={Boolean(touched && errors.cpf) || undefined} aria-describedby={touched && errors.cpf ? "reactivate-cpf-error" : undefined} /></FormField>
          <FormField id="reactivate-email" label={t("auth.email")} error={touched ? errors.email : undefined} required><Input id="reactivate-email" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} aria-invalid={Boolean(touched && errors.email) || undefined} aria-describedby={touched && errors.email ? "reactivate-email-error" : undefined} /></FormField>
          <FormField id="reactivate-password" label={t("auth.password")} error={touched ? errors.password : undefined} required><PasswordField id="reactivate-password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} error={Boolean(touched && errors.password)} aria-describedby={touched && errors.password ? "reactivate-password-error" : undefined} /></FormField>
          {error && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}
          <LoadingButton type="submit" loading={loading} loadingLabel={t("auth.reactivate.loading")} disabled={!valid} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("auth.reactivate.submit")}</LoadingButton>
          <Button type="button" variant="outline" className="w-full" disabled={!isCpfShapeValid(cpf) || !isValidEmail(email)} onClick={() => setPasswordDialog(true)}><KeyRound className="mr-2 h-4 w-4" />{lang === "pt-BR" ? "Alterar senha sem reativar" : "Change password without reactivating"}</Button>
          <p className="text-center text-xs text-muted-foreground"><Link to="/login" className="font-medium text-vinho underline-offset-4 hover:underline">{t("auth.reset.backLogin")}</Link></p>
        </form>
      )}

      <Dialog open={passwordDialog} onOpenChange={setPasswordDialog}><DialogContent><DialogHeader><DialogTitle>{lang === "pt-BR" ? "Alterar senha da conta desativada" : "Change suspended account password"}</DialogTitle><DialogDescription>{lang === "pt-BR" ? "A conta continuará desativada. Por segurança, informe a senha atual." : "The account will remain suspended. For security, enter the current password."}</DialogDescription></DialogHeader><div className="space-y-4"><FormField id="suspended-current-password" label={lang === "pt-BR" ? "Senha atual" : "Current password"} required><PasswordField id="suspended-current-password" autoComplete="current-password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} /></FormField><FormField id="suspended-new-password" label={t("auth.newPassword")} required><PasswordField id="suspended-new-password" autoComplete="new-password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} /></FormField><PasswordRequirements password={newPassword} /><FormField id="suspended-confirm-password" label={t("auth.confirmPassword")} required><PasswordField id="suspended-confirm-password" autoComplete="new-password" value={confirmNewPassword} onChange={(e) => setConfirmNewPassword(e.target.value)} /></FormField>{confirmNewPassword && newPassword !== confirmNewPassword && <p className="text-xs text-destructive">{t("auth.validation.passwordMatch")}</p>}</div><DialogFooter><Button variant="outline" onClick={() => setPasswordDialog(false)}>{t("common.cancel")}</Button><LoadingButton loading={changingPassword} onClick={() => void submitPasswordChange()} disabled={!passwordChangeValid} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("auth.reset.update")}</LoadingButton></DialogFooter></DialogContent></Dialog>
    </AuthShell>
  );
}
