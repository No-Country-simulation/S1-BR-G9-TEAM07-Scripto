import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { AlertTriangle, CheckCircle2 } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { CpfInput } from "@/components/CpfInput";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PasswordField } from "@/components/PasswordField";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { isCpfShapeValid, isValidEmail } from "@/lib/validation";
import { reactivateAccount } from "@/services/auth.service";

export const Route = createFileRoute("/account-suspended")({ component: ReactivationPage });

function ReactivationPage() {
  const { t } = useI18n();
  const router = useRouter();
  const [cpf, setCpf] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [touched, setTouched] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const errors = useMemo(() => ({ cpf: isCpfShapeValid(cpf) ? "" : t("auth.validation.cpf"), email: isValidEmail(email) ? "" : t("auth.validation.email"), password: password ? "" : t("auth.validation.required") }), [cpf, email, password, t]);
  const valid = !errors.cpf && !errors.email && !errors.password;

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setTouched(true);
    if (!valid) return;
    setError(null);
    setLoading(true);
    try {
      // CPF is intentionally not added to the request: the current backend DTO accepts email and password only.
      await reactivateAccount({ email: email.trim().toLowerCase(), password });
      setSuccess(true);
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell title={t("auth.reactivate.title")} subtitle={t("auth.reactivate.subtitle")}>
      <div className="mb-5 flex items-start gap-3 rounded-lg border border-dourado/40 bg-dourado/10 p-4 text-sm">
        <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-dourado" aria-hidden="true" />
        <p className="leading-relaxed text-muted-foreground">{t("auth.reactivate.notice")}</p>
      </div>
      {success ? (
        <div className="space-y-4 text-center">
          <CheckCircle2 className="mx-auto h-10 w-10 text-verde" aria-hidden="true" />
          <p className="text-sm">{t("auth.reactivate.success")}</p>
          <Button className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90" onClick={() => void router.navigate({ to: "/login" })}>{t("auth.reactivate.goLogin")}</Button>
        </div>
      ) : (
        <form onSubmit={submit} className="space-y-4" noValidate>
          <FormField id="reactivate-cpf" label={t("auth.cpf")} error={touched ? errors.cpf : undefined} required>
            <CpfInput id="reactivate-cpf" value={cpf} onValueChange={setCpf} aria-invalid={Boolean(touched && errors.cpf) || undefined} aria-describedby={touched && errors.cpf ? "reactivate-cpf-error" : undefined} />
          </FormField>
          <FormField id="reactivate-email" label={t("auth.email")} error={touched ? errors.email : undefined} required>
            <Input id="reactivate-email" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} aria-invalid={Boolean(touched && errors.email) || undefined} aria-describedby={touched && errors.email ? "reactivate-email-error" : undefined} />
          </FormField>
          <FormField id="reactivate-password" label={t("auth.password")} error={touched ? errors.password : undefined} required>
            <PasswordField id="reactivate-password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} error={Boolean(touched && errors.password)} aria-describedby={touched && errors.password ? "reactivate-password-error" : undefined} />
          </FormField>
          {error && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}
          <LoadingButton type="submit" loading={loading} loadingLabel={t("auth.reactivate.loading")} disabled={!valid} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("auth.reactivate.submit")}</LoadingButton>
          <p className="text-center text-xs text-muted-foreground"><Link to="/login" className="font-medium text-vinho underline-offset-4 hover:underline">{t("auth.reset.backLogin")}</Link></p>
        </form>
      )}
    </AuthShell>
  );
}
