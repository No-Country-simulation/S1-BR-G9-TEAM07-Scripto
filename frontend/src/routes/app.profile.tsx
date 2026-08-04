import { createFileRoute, useRouter } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import {
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle,
} from "@/components/ui/dialog";
import { currentUser, updateProfile, scheduleDeletion, logout } from "@/services/auth.service";
import { listMine, type Doc } from "@/services/documents.service";

export const Route = createFileRoute("/app/profile")({ component: ProfilePage });

const maskCpf = (v: string) => v.replace(/\D/g, "").replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");

function ProfilePage() {
  const router = useRouter();
  const user = currentUser();
  const [name, setName] = useState(user?.fullName ?? "");
  const [email, setEmail] = useState(user?.email ?? "");
  const [docs, setDocs] = useState<Doc[]>([]);
  const [confirmDelete, setConfirmDelete] = useState(false);

  useEffect(() => { listMine().then(setDocs); }, []);
  if (!user) return null;

  const total = docs.length;
  const processed = docs.filter(d => d.status === "PROCESSED").length;
  const topCategory = topOf(docs.map(d => d.category)) ?? "—";
  const topLevel = topOf(docs.map(d => d.level)) ?? "—";
  const initials = name.split(" ").filter(Boolean).slice(0, 2).map(s => s[0]?.toUpperCase()).join("");

  return (
    <div className="mx-auto max-w-4xl px-6 py-10">
      <header>
        <p className="text-xs uppercase tracking-widest text-dourado">Você</p>
        <h1 className="font-serif text-4xl">Perfil</h1>
      </header>
      <OrnamentDivider className="mt-6" />

      <section className="mt-8 grid gap-6 md:grid-cols-[auto_1fr] md:items-start">
        <div className="flex h-24 w-24 items-center justify-center rounded-full bg-pessego text-3xl font-serif text-marrom">
          {initials || "?"}
        </div>
        <div className="space-y-4">
          <div>
            <Label htmlFor="pname">Nome</Label>
            <Input id="pname" value={name} onChange={e => setName(e.target.value)} className="mt-1" />
          </div>
          <div>
            <Label htmlFor="pemail">E-mail</Label>
            <Input id="pemail" type="email" value={email} onChange={e => setEmail(e.target.value)} className="mt-1" />
          </div>
          <div>
            <Label htmlFor="pcpf">CPF</Label>
            <Input id="pcpf" value={maskCpf(user.cpf)} readOnly disabled className="mt-1" />
          </div>
          <Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90"
            onClick={() => updateProfile({ name, email })}>Salvar alterações</Button>
        </div>
      </section>

      <section className="mt-12">
        <h2 className="font-serif text-2xl">Estatísticas</h2>
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Stat label="Envios totais" value={total} />
          <Stat label="Análises concluídas" value={processed} />
          <Stat label="Categoria mais frequente" value={topCategory} />
          <Stat label="Nível predominante" value={topLevel} />
        </div>
      </section>

      <section className="mt-12 rounded-xl border border-vinho/30 bg-vinho/5 p-6">
        <div className="flex items-start gap-3">
          <AlertTriangle className="mt-1 h-5 w-5 text-vinho" />
          <div>
            <h3 className="font-serif text-xl text-vinho">Excluir conta</h3>
            <p className="mt-1 text-sm text-taupe">
              A conta ficará marcada para exclusão por 30 dias. Nesse período você pode reativá-la a qualquer momento.
            </p>
            <Button variant="outline" onClick={() => setConfirmDelete(true)}
              className="mt-4 border-vinho text-vinho hover:bg-vinho/10">
              Excluir minha conta
            </Button>
          </div>
        </div>
      </section>

      <Dialog open={confirmDelete} onOpenChange={setConfirmDelete}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Excluir sua conta?</DialogTitle>
            <DialogDescription>
              Sua conta entrará em processo de exclusão por 30 dias. Depois disso, todos os dados serão removidos.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmDelete(false)}>Cancelar</Button>
            <Button className="bg-vinho text-vinho-foreground"
              onClick={async () => { await scheduleDeletion(); logout(); router.navigate({ to: "/" }); }}>
              Confirmar exclusão
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-xl border border-bege bg-card p-5">
      <p className="text-xs uppercase tracking-widest text-taupe">{label}</p>
      <p className="mt-2 font-serif text-3xl">{value}</p>
    </div>
  );
}

function topOf(arr: string[]) {
  const c = new Map<string, number>();
  arr.forEach(v => v && v !== "—" && c.set(v, (c.get(v) ?? 0) + 1));
  let best: string | null = null; let n = 0;
  c.forEach((v, k) => { if (v > n) { n = v; best = k; } });
  return best;
}
