import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { ShieldCheck } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PasswordField } from "@/components/PasswordField";
import { Input } from "@/components/ui/input";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { isValidEmail } from "@/lib/validation";
import { ApiError } from "@/services/api";
import { loginAdmin } from "@/services/auth.service";

export const Route = createFileRoute("/admin/login")({ component: AdminLogin });

function AdminLogin() {
  const { t } = useI18n();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [touched, setTouched] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const errors = useMemo(() => ({ email: isValidEmail(email) ? "" : t("auth.validation.email"), password: password ? "" : t("auth.validation.required") }), [email, password, t]);
  const valid = !errors.email && !errors.password;

  async function submit(event: React.FormEvent) {
    event.preventDefault(); setTouched(true); if (!valid) return;
    setError(null); setLoading(true);
    try { await loginAdmin({ email: email.trim().toLowerCase(), password }); await router.navigate({ to: "/admin" }); }
    catch (currentError) {
      if (currentError instanceof ApiError && (currentError.status === 401 || currentError.status === 404)) setError(t("auth.login.invalid"));
      else setError(friendlyError(currentError, t));
    }
    finally { setLoading(false); }
  }

  return (
    <AuthShell title={t("auth.admin.title")} subtitle={t("auth.admin.subtitle")} footer={<Link to="/" className="font-medium text-vinho underline-offset-4 hover:underline">{t("legal.back")}</Link>}>
      <form onSubmit={submit} className="space-y-4" noValidate>
        <div className="flex items-start gap-2 rounded-lg border border-dourado/40 bg-dourado/10 p-3 text-sm text-muted-foreground"><ShieldCheck className="mt-0.5 h-4 w-4 shrink-0 text-dourado" aria-hidden="true" />{t("auth.admin.notice")}</div>
        <FormField id="admin-email" label={t("auth.email")} error={touched ? errors.email : undefined} required><Input id="admin-email" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} aria-invalid={Boolean(touched && errors.email) || undefined} /></FormField>
        <FormField id="admin-password" label={t("auth.password")} error={touched ? errors.password : undefined} required><PasswordField id="admin-password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} error={Boolean(touched && errors.password)} /></FormField>
        {error && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}
        <LoadingButton type="submit" loading={loading} loadingLabel={t("auth.admin.loading")} disabled={!valid} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("auth.admin.submit")}</LoadingButton>
      </form>
    </AuthShell>
  );
}
