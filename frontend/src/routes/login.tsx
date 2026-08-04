import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useI18n } from "@/lib/i18n";
import { login } from "@/services/auth.service";

export const Route = createFileRoute("/login")({ component: LoginPage });

function LoginPage() {
  const { t } = useI18n();
  const router = useRouter();
  const [email, setEmail] = useState(""); const [password, setPassword] = useState("");
  const [show, setShow] = useState(false); const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault(); setErr(null); setLoading(true);
    try {
      const u = await login(email, password);
      if (u.deletionScheduledAt) router.navigate({ to: "/account-suspended" });
      else router.navigate({ to: "/app" });
    } catch (e) { setErr((e as Error).message); } finally { setLoading(false); }
  }

  return (
    <AuthShell
      title={t("auth.login.title")} subtitle={t("auth.login.subtitle")}
      footer={<>{t("auth.no")} <Link to="/register" className="font-medium text-vinho hover:underline">{t("nav.register")}</Link></>}
    >
      <form onSubmit={submit} className="space-y-4">
        <div>
          <Label htmlFor="email">{t("auth.email")}</Label>
          <Input id="email" type="email" required value={email} onChange={e => setEmail(e.target.value)} className="mt-1" />
        </div>
        <div>
          <Label htmlFor="password">{t("auth.password")}</Label>
          <div className="relative mt-1">
            <Input id="password" type={show ? "text" : "password"} required value={password} onChange={e => setPassword(e.target.value)} />
            <button type="button" onClick={() => setShow(s => !s)} aria-label="Mostrar senha"
              className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-muted-foreground hover:text-foreground">
              {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
        </div>
        {err && <p role="alert" className="text-sm text-vinho">{err}</p>}
        <Button type="submit" disabled={loading} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">
          {loading ? "Entrando…" : t("auth.submit.login")}
        </Button>
      </form>
    </AuthShell>
  );
}
