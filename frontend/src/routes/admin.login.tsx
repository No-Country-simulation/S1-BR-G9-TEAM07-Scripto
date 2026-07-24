import { createFileRoute, useRouter } from "@tanstack/react-router";
import { useState } from "react";
import { ShieldCheck } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { adminLogin } from "@/services/auth.service";

export const Route = createFileRoute("/admin/login")({ component: AdminLogin });

function AdminLogin() {
  const router = useRouter();
  const [email, setEmail] = useState(""); const [password, setPassword] = useState(""); const [err, setErr] = useState<string | null>(null);
  return (
    <AuthShell title="Acesso restrito" subtitle="Painel administrativo · Scripto">
      <form className="space-y-4" onSubmit={async e => {
        e.preventDefault();
        try { await adminLogin(email, password); router.navigate({ to: "/admin" }); }
        catch (e) { setErr((e as Error).message); }
      }}>
        <div className="flex items-center gap-2 rounded-lg border border-dourado/40 bg-dourado/10 p-3 text-sm">
          <ShieldCheck className="h-4 w-4 text-dourado" /> Ambiente de moderação (mock).
        </div>
        <div>
          <Label htmlFor="ae">E-mail</Label>
          <Input id="ae" type="email" required value={email} onChange={e => setEmail(e.target.value)} className="mt-1" />
        </div>
        <div>
          <Label htmlFor="ap">Senha</Label>
          <Input id="ap" type="password" required value={password} onChange={e => setPassword(e.target.value)} className="mt-1" />
        </div>
        {err && <p className="text-sm text-vinho">{err}</p>}
        <Button type="submit" className="w-full bg-vinho text-vinho-foreground">Entrar como administrador</Button>
      </form>
    </AuthShell>
  );
}
