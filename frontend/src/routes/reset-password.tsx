import { createFileRoute, Link } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { CheckCircle2 } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { CpfInput } from "@/components/CpfInput";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PasswordField } from "@/components/PasswordField";
import { PasswordRequirements } from "@/components/PasswordRequirements";
import { Input } from "@/components/ui/input";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { isCpfShapeValid, isStrongPassword, isValidEmail } from "@/lib/validation";
import { resetPassword, verifyResetIdentity } from "@/services/auth.service";

export const Route = createFileRoute("/reset-password")({ component: ResetPasswordPage });

function ResetPasswordPage() {
  const { t } = useI18n();
  const [step, setStep] = useState<1 | 2>(1);
  const [cpf, setCpf] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [touched, setTouched] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [complete, setComplete] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [updating, setUpdating] = useState(false);

  const identityValid = isCpfShapeValid(cpf) && isValidEmail(email);
  const passwordValid = isStrongPassword(password) && password === confirm;
  const passwordErrors = useMemo(() => ({
    password: isStrongPassword(password) ? "" : t("auth.validation.password"),
    confirm: password === confirm ? "" : t("auth.validation.passwordMatch"),
  }), [password, confirm, t]);

  async function verifyIdentity(event: React.FormEvent) {
    event.preventDefault();
    setTouched(true);
    setMessage(null);
    if (!identityValid) return;
    setVerifying(true);
    try {
      await verifyResetIdentity({ cpf, email });
      setTouched(false);
      setStep(2);
    } catch (currentError) {
      setMessage(friendlyError(currentError, t));
    } finally {
      setVerifying(false);
    }
  }

  async function updatePassword(event: React.FormEvent) {
    event.preventDefault();
    setTouched(true);
    setMessage(null);
    if (!passwordValid) return;
    setUpdating(true);
    try {
      await resetPassword({ cpf, email, newPassword: password, confirmNewPassword: confirm });
      setComplete(true);
    } catch (currentError) {
      setMessage(friendlyError(currentError, t));
    } finally {
      setUpdating(false);
    }
  }

  return (
    <AuthShell
      title={t("auth.reset.title")}
      subtitle={t("auth.reset.subtitle")}
      footer={<Link to="/login" className="font-medium text-vinho underline-offset-4 hover:underline">{t("auth.reset.backLogin")}</Link>}
    >
      <div className="mb-5 grid grid-cols-2 gap-2" aria-label={`${t("common.page")} ${step} ${t("common.of")} 2`}>
        <div className={`rounded-lg border p-3 text-xs ${step === 1 ? "border-vinho bg-vinho/5 text-vinho" : "border-border text-muted-foreground"}`}><strong>1.</strong> {t("auth.reset.step1")}</div>
        <div className={`rounded-lg border p-3 text-xs ${step === 2 ? "border-vinho bg-vinho/5 text-vinho" : "border-border text-muted-foreground"}`}><strong>2.</strong> {t("auth.reset.step2")}</div>
      </div>

      {complete ? (
        <div className="space-y-4 text-center">
          <CheckCircle2 className="mx-auto h-10 w-10 text-verde" aria-hidden="true" />
          <p className="text-sm">{t("auth.reset.success")}</p>
          <Link to="/login" className="inline-flex w-full items-center justify-center rounded-md bg-vinho px-4 py-2 text-sm font-medium text-vinho-foreground hover:bg-vinho/90">{t("auth.reset.backLogin")}</Link>
        </div>
      ) : step === 1 ? (
        <form onSubmit={verifyIdentity} className="space-y-4" noValidate>
          <FormField id="reset-cpf" label={t("auth.cpf")} error={touched && !isCpfShapeValid(cpf) ? t("auth.validation.cpf") : undefined} required>
            <CpfInput id="reset-cpf" value={cpf} onValueChange={setCpf} aria-invalid={Boolean(touched && !isCpfShapeValid(cpf)) || undefined} aria-describedby={touched && !isCpfShapeValid(cpf) ? "reset-cpf-error" : undefined} />
          </FormField>
          <FormField id="reset-email" label={t("auth.email")} error={touched && !isValidEmail(email) ? t("auth.validation.email") : undefined} required>
            <Input id="reset-email" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} aria-invalid={Boolean(touched && !isValidEmail(email)) || undefined} aria-describedby={touched && !isValidEmail(email) ? "reset-email-error" : undefined} />
          </FormField>
          {message && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{message}</p>}
          <LoadingButton type="submit" loading={verifying} loadingLabel={t("auth.reset.verify")} disabled={!identityValid} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("auth.reset.verify")}</LoadingButton>
        </form>
      ) : (
        <form onSubmit={updatePassword} className="space-y-4" noValidate>
          <FormField id="reset-new-password" label={t("auth.newPassword")} error={touched ? passwordErrors.password : undefined} required>
            <PasswordField id="reset-new-password" autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} error={Boolean(touched && passwordErrors.password)} aria-describedby={touched && passwordErrors.password ? "reset-new-password-error reset-password-requirements" : "reset-password-requirements"} />
          </FormField>
          <div id="reset-password-requirements"><PasswordRequirements password={password} /></div>
          <FormField id="reset-confirm-password" label={t("auth.confirmPassword")} error={touched ? passwordErrors.confirm : undefined} required>
            <PasswordField id="reset-confirm-password" autoComplete="new-password" value={confirm} onChange={(event) => setConfirm(event.target.value)} error={Boolean(touched && passwordErrors.confirm)} aria-describedby={touched && passwordErrors.confirm ? "reset-confirm-password-error" : undefined} />
          </FormField>
          {message && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{message}</p>}
          <LoadingButton type="submit" loading={updating} loadingLabel={t("auth.reset.update")} disabled={!passwordValid} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("auth.reset.update")}</LoadingButton>
        </form>
      )}
    </AuthShell>
  );
}
