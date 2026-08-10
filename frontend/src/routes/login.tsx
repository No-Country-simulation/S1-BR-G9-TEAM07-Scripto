import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { AuthShell } from "@/components/auth/AuthShell";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { PasswordField } from "@/components/PasswordField";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/services/api";
import { loginUser } from "@/services/auth.service";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { isValidEmail } from "@/lib/validation";

export const Route = createFileRoute("/login")({ component: LoginPage });

function LoginPage() {
  const { t } = useI18n();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [touched, setTouched] = useState({ email: false, password: false });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const errors = useMemo(() => ({
    email: !email.trim() ? t("auth.validation.required") : !isValidEmail(email) ? t("auth.validation.email") : "",
    password: !password ? t("auth.validation.required") : "",
  }), [email, password, t]);
  const valid = !errors.email && !errors.password;

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setTouched({ email: true, password: true });
    if (!valid) return;
    setError(null);
    setLoading(true);
    try {
      await loginUser({ email: email.trim().toLowerCase(), password });
      await router.navigate({ to: "/app" });
    } catch (currentError) {
      if (currentError instanceof ApiError && currentError.status === 403) {
        toast.error(friendlyError(currentError, t));
        await router.navigate({ to: "/account-suspended" });
        return;
      }
      if (currentError instanceof ApiError && (currentError.status === 401 || currentError.status === 404)) {
        setError(t("auth.login.invalid"));
        return;
      }
      setError(friendlyError(currentError, t));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title={t("auth.login.title")}
      subtitle={t("auth.login.subtitle")}
      footer={<>{t("auth.login.noAccount")} <Link to="/register" className="font-semibold text-vinho underline-offset-4 hover:underline">{t("nav.register")}</Link></>}
    >
      <form onSubmit={submit} className="space-y-4" noValidate>
        <FormField id="email" label={t("auth.email")} error={touched.email ? errors.email : undefined} required>
          <Input
            id="email"
            type="email"
            autoComplete="email"
            value={email}
            onBlur={() => setTouched((current) => ({ ...current, email: true }))}
            onChange={(event) => setEmail(event.target.value)}
            aria-invalid={Boolean(touched.email && errors.email) || undefined}
            aria-describedby={touched.email && errors.email ? "email-error" : undefined}
          />
        </FormField>
        <FormField id="password" label={t("auth.password")} error={touched.password ? errors.password : undefined} required>
          <PasswordField
            id="password"
            autoComplete="current-password"
            value={password}
            onBlur={() => setTouched((current) => ({ ...current, password: true }))}
            onChange={(event) => setPassword(event.target.value)}
            error={Boolean(touched.password && errors.password)}
            aria-describedby={touched.password && errors.password ? "password-error" : undefined}
          />
        </FormField>
        <div className="text-right text-xs">
          <Link to="/reset-password" className="font-medium text-vinho underline-offset-4 hover:underline">{t("auth.login.forgot")}</Link>
        </div>
        {error && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}
        <LoadingButton type="submit" loading={loading} loadingLabel={t("auth.login.loading")} disabled={!valid} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">
          {t("auth.login.submit")}
        </LoadingButton>
      </form>
    </AuthShell>
  );
}
