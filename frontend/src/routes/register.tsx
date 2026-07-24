import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { useI18n } from "@/lib/i18n";
import { register } from "@/services/auth.service";

const maskCpf = (v: string) => v.replace(/\D/g, "").slice(0, 11)
  .replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2");

export const Route = createFileRoute("/register")({ component: RegisterPage });

function RegisterPage() {
  const { t } = useI18n();
  const router = useRouter();
  const [form, setForm] = useState({ name: "", cpf: "", email: "", password: "", confirm: "" });
  const [show, setShow] = useState(false); const [terms, setTerms] = useState(false);
  const [err, setErr] = useState<string | null>(null); const [loading, setLoading] = useState(false);
  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => setForm({ ...form, [k]: e.target.value });

  async function submit(e: React.FormEvent) {
    e.preventDefault(); setErr(null);
    if (!terms) return setErr("É necessário aceitar os termos.");
    if (form.password !== form.confirm) return setErr("As senhas não coincidem.");
    if (form.cpf.replace(/\D/g, "").length !== 11) return setErr("CPF inválido.");
    setLoading(true);
    try { await register(form); router.navigate({ to: "/app" }); }
    catch (e) { setErr((e as Error).message); } finally { setLoading(false); }
  }

  return (
    <AuthShell
      title={t("auth.register.title")} subtitle={t("auth.register.subtitle")}
      footer={<>{t("auth.have")} <Link to="/login" className="font-medium text-vinho hover:underline">{t("nav.login")}</Link></>}
    >
      <form onSubmit={submit} className="space-y-4">
        <div>
          <Label htmlFor="name">{t("auth.name")}</Label>
          <Input id="name" required value={form.name} onChange={set("name")} className="mt-1" />
        </div>
        <div>
          <Label htmlFor="cpf">{t("auth.cpf")}</Label>
          <Input id="cpf" required inputMode="numeric" placeholder="000.000.000-00"
            value={form.cpf} onChange={e => setForm({ ...form, cpf: maskCpf(e.target.value) })} className="mt-1" />
        </div>
        <div>
          <Label htmlFor="email">{t("auth.email")}</Label>
          <Input id="email" type="email" required value={form.email} onChange={set("email")} className="mt-1" />
        </div>
        <div>
          <Label htmlFor="password">{t("auth.password")}</Label>
          <div className="relative mt-1">
            <Input id="password" type={show ? "text" : "password"} required minLength={6} value={form.password} onChange={set("password")} />
            <button type="button" onClick={() => setShow(s => !s)} aria-label="Mostrar senha"
              className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-muted-foreground hover:text-foreground">
              {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
        </div>
        <div>
          <Label htmlFor="confirm">{t("auth.confirm")}</Label>
          <Input id="confirm" type={show ? "text" : "password"} required value={form.confirm} onChange={set("confirm")} className="mt-1" />
        </div>
        <label className="flex items-start gap-2 text-sm text-taupe">
          <Checkbox checked={terms} onCheckedChange={v => setTerms(!!v)} />
          <span>{t("auth.terms")}</span>
        </label>
        {err && <p role="alert" className="text-sm text-vinho">{err}</p>}
        <Button type="submit" disabled={loading} className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">
          {loading ? "Criando…" : t("auth.submit.register")}
        </Button>
      </form>
    </AuthShell>
  );
}
