import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { AuthShell } from "@/components/auth/AuthShell";
import { CpfInput } from "@/components/CpfInput";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PasswordField } from "@/components/PasswordField";
import { PasswordRequirements } from "@/components/PasswordRequirements";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { isCpfShapeValid, isStrongPassword, isValidEmail } from "@/lib/validation";
import { fieldError } from "@/services/api";
import { registerUser } from "@/services/auth.service";

export const Route = createFileRoute("/register")({ component: RegisterPage });

function RegisterPage() {
  const { t } = useI18n();
  const router = useRouter();
  const [form, setForm] = useState({ fullName: "", cpf: "", email: "", password: "", confirm: "" });
  const [terms, setTerms] = useState(false);
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const [serverFields, setServerFields] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const localErrors = useMemo(() => ({
    fullName: form.fullName.trim().split(/\s+/).length < 2 ? t("auth.validation.name") : "",
    cpf: !isCpfShapeValid(form.cpf) ? t("auth.validation.cpf") : "",
    email: !isValidEmail(form.email) ? t("auth.validation.email") : "",
    password: !isStrongPassword(form.password) ? t("auth.validation.password") : "",
    confirm: form.confirm !== form.password ? t("auth.validation.passwordMatch") : "",
    terms: !terms ? t("auth.validation.terms") : "",
  }), [form, terms, t]);
  const valid = Object.values(localErrors).every((value) => !value);

  const set = (key: keyof typeof form) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setForm((current) => ({ ...current, [key]: event.target.value }));
    setServerFields((current) => ({ ...current, [key]: "" }));
  };
  const blur = (key: string) => () => setTouched((current) => ({ ...current, [key]: true }));
  const shownError = (key: keyof typeof localErrors) => serverFields[key] || (touched[key] ? localErrors[key] : "");

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setTouched({ fullName: true, cpf: true, email: true, password: true, confirm: true, terms: true });
    if (!valid) return;
    setError(null);
    setServerFields({});
    setLoading(true);
    try {
      await registerUser({ fullName: form.fullName, cpf: form.cpf, email: form.email, password: form.password });
      await router.navigate({ to: "/app" });
    } catch (currentError) {
      setServerFields({
        fullName: fieldError(currentError, "fullName") || "",
        cpf: fieldError(currentError, "cpf") || "",
        email: fieldError(currentError, "email") || "",
        password: fieldError(currentError, "password") || "",
      });
      setError(friendlyError(currentError, t));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title={t("auth.register.title")}
      subtitle={t("auth.register.subtitle")}
      footer={<>{t("auth.register.haveAccount")} <Link to="/login" className="font-semibold text-vinho underline-offset-4 hover:underline">{t("nav.login")}</Link></>}
    >
      <form onSubmit={submit} className="space-y-4" noValidate>
        <FormField id="fullName" label={t("auth.fullName")} error={shownError("fullName")} required>
          <Input id="fullName" autoComplete="name" maxLength={150} value={form.fullName} onBlur={blur("fullName")} onChange={set("fullName")} aria-invalid={Boolean(shownError("fullName")) || undefined} aria-describedby={shownError("fullName") ? "fullName-error" : undefined} />
        </FormField>
        <FormField id="cpf" label={t("auth.cpf")} error={shownError("cpf")} required>
          <CpfInput id="cpf" value={form.cpf} onBlur={blur("cpf")} onValueChange={(cpf) => { setForm((current) => ({ ...current, cpf })); setServerFields((current) => ({ ...current, cpf: "" })); }} aria-invalid={Boolean(shownError("cpf")) || undefined} aria-describedby={shownError("cpf") ? "cpf-error" : undefined} />
        </FormField>
        <FormField id="email" label={t("auth.email")} error={shownError("email")} required>
          <Input id="email" type="email" autoComplete="email" maxLength={255} value={form.email} onBlur={blur("email")} onChange={set("email")} aria-invalid={Boolean(shownError("email")) || undefined} aria-describedby={shownError("email") ? "email-error" : undefined} />
        </FormField>
        <FormField id="password" label={t("auth.password")} error={shownError("password")} required>
          <PasswordField id="password" autoComplete="new-password" minLength={8} maxLength={15} value={form.password} onBlur={blur("password")} onChange={set("password")} error={Boolean(shownError("password"))} aria-describedby={shownError("password") ? "password-error password-requirements" : "password-requirements"} />
        </FormField>
        <div id="password-requirements"><PasswordRequirements password={form.password} /></div>
        <FormField id="confirm" label={t("auth.confirmPassword")} error={shownError("confirm")} required>
          <PasswordField id="confirm" autoComplete="new-password" value={form.confirm} onBlur={blur("confirm")} onChange={set("confirm")} error={Boolean(shownError("confirm"))} aria-describedby={shownError("confirm") ? "confirm-error" : undefined} />
        </FormField>
        <div>
          <label className="flex cursor-pointer items-start gap-3 rounded-lg border border-border p-3 text-sm leading-relaxed transition hover:bg-muted/30">
            <Checkbox checked={terms} onCheckedChange={(value) => { setTerms(Boolean(value)); setTouched((current) => ({ ...current, terms: true })); }} aria-invalid={Boolean(shownError("terms")) || undefined} />
            <span className="text-muted-foreground">
              {t("auth.register.consentPrefix")} <Link to="/terms" className="font-medium text-vinho underline-offset-4 hover:underline">{t("auth.register.terms")}</Link> {t("auth.register.consentJoin")} <Link to="/privacity" className="font-medium text-vinho underline-offset-4 hover:underline">{t("auth.register.privacy")}</Link>.
            </span>
          </label>
          {shownError("terms") && <p id="terms-error" role="alert" className="mt-1.5 text-xs font-medium text-destructive">{shownError("terms")}</p>}
        </div>
        {error && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}
        <LoadingButton type="submit" loading={loading} loadingLabel={t("auth.register.loading")} disabled={!valid} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("auth.register.submit")}</LoadingButton>
      </form>
    </AuthShell>
  );
}
