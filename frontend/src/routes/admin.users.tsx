import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import { isSuspended, toggleSuspend } from "@/services/admin.service";
import type { User } from "@/services/auth.service";
import type { Doc } from "@/services/documents.service";

export const Route = createFileRoute("/admin/users")({ component: AdminUsers });

function AdminUsers() {
  const [q, setQ] = useState(""); const [, force] = useState(0);
  const users: User[] = JSON.parse(typeof window === "undefined" ? "[]" : localStorage.getItem("scripto-users") || "[]");
  const docs: Doc[] = JSON.parse(typeof window === "undefined" ? "[]" : localStorage.getItem("scripto-docs") || "[]");

  const filtered = useMemo(() => users.filter(u =>
    !q || u.fullName.toLowerCase().includes(q.toLowerCase()) || u.email.toLowerCase().includes(q.toLowerCase())
  ), [users, q]);

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <p className="text-xs uppercase tracking-widest text-dourado">Gestão</p>
      <h1 className="font-serif text-4xl">Usuários</h1>
      <OrnamentDivider className="mt-6" />
      <div className="relative mt-6 max-w-md">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-taupe" />
        <Input placeholder="Buscar por nome ou e-mail…" value={q} onChange={e => setQ(e.target.value)} className="pl-9" />
      </div>

      <div className="mt-6 overflow-hidden rounded-xl border border-bege">
        <table className="w-full text-sm">
          <thead className="bg-muted text-taupe">
            <tr>
              <th className="p-3 text-left">Nome</th><th className="p-3 text-left">E-mail</th>
              <th className="p-3 text-left">Docs</th><th className="p-3 text-left">Status</th>
              <th className="p-3 text-right">Ações</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 && <tr><td colSpan={5} className="p-6 text-center text-taupe">Nenhum usuário.</td></tr>}
            {filtered.map(u => {
              const suspended = isSuspended(u.id);
              const count = docs.filter(d => d.ownerId === u.id).length;
              return (
                <tr key={u.id} className="border-t border-border">
                  <td className="p-3">{u.fullName}</td>
                  <td className="p-3 text-taupe">{u.email}</td>
                  <td className="p-3">{count}</td>
                  <td className="p-3">
                    {suspended ? <span className="rounded bg-vinho/10 px-2 py-0.5 text-xs text-vinho">Suspenso</span>
                               : <span className="rounded bg-verde/10 px-2 py-0.5 text-xs text-verde">Ativo</span>}
                  </td>
                  <td className="p-3 text-right">
                    <Button size="sm" variant="outline"
                      onClick={() => { toggleSuspend(u.id); force(x => x + 1); }}>
                      {suspended ? "Reativar" : "Suspender"}
                    </Button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
