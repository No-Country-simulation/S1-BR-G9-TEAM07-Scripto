import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Users, FileText, Flag, ShieldOff } from "lucide-react";
import { metrics } from "@/services/admin.service";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";

export const Route = createFileRoute("/admin/")({ component: AdminOverview });

function AdminOverview() {
  const [m, setM] = useState<Awaited<ReturnType<typeof metrics>> | null>(null);
  useEffect(() => { metrics().then(setM); }, []);

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <p className="text-xs uppercase tracking-widest text-dourado">Painel</p>
      <h1 className="font-serif text-4xl">Visão geral</h1>
      <OrnamentDivider className="mt-6" />
      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card icon={Users}   label="Usuários"           value={m?.users ?? "—"} />
        <Card icon={ShieldOff} label="Suspensos"        value={m?.suspended ?? "—"} />
        <Card icon={FileText} label="Documentos"        value={m?.docs ?? "—"} />
        <Card icon={Flag}    label="Denúncias pendentes" value={m?.reportsPending ?? "—"} />
      </div>
    </div>
  );
}

function Card({ icon: Icon, label, value }: { icon: typeof Users; label: string; value: string | number }) {
  return (
    <div className="rounded-xl border border-bege bg-card p-5">
      <div className="flex items-center gap-2 text-taupe"><Icon className="h-4 w-4" /> <span className="text-xs uppercase tracking-widest">{label}</span></div>
      <p className="mt-3 font-serif text-3xl">{value}</p>
    </div>
  );
}
