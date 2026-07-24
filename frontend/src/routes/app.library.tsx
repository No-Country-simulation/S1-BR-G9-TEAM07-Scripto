import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { Filter, RefreshCw, Trash2, Pencil, EyeOff } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { DocCard } from "@/components/common/DocCard";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import {
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle,
} from "@/components/ui/dialog";
import { listMine, remove, rename, retry, setPublic, type Doc } from "@/services/documents.service";

export const Route = createFileRoute("/app/library")({ component: LibraryPage });

function LibraryPage() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const [level, setLevel] = useState<string>(""); const [cat, setCat] = useState<string>(""); const [tag, setTag] = useState<string>("");
  const [renaming, setRenaming] = useState<Doc | null>(null); const [renameValue, setRenameValue] = useState(""); const [renameErr, setRenameErr] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<Doc | null>(null);

  const load = () => listMine().then(setDocs);
  useEffect(() => { load(); const i = setInterval(load, 1500); return () => clearInterval(i); }, []);

  const filtered = useMemo(() => docs.filter(d =>
    (!level || d.level === level) && (!cat || d.category === cat) && (!tag || d.tags.includes(tag))
  ), [docs, level, cat, tag]);

  const cats = [...new Set(docs.map(d => d.category).filter(c => c && c !== "—"))];
  const tags = [...new Set(docs.flatMap(d => d.tags))];

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <header className="flex items-end justify-between gap-4">
        <div>
          <p className="text-xs uppercase tracking-widest text-dourado">Sua estante</p>
          <h1 className="font-serif text-4xl">Minha biblioteca</h1>
        </div>
        <Link to="/app"><Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90">Novo documento</Button></Link>
      </header>
      <OrnamentDivider className="mt-6" />

      {docs.length > 0 && (
        <div className="mt-6 flex flex-wrap items-center gap-3 rounded-xl border border-bege bg-muted/30 p-3 text-sm">
          <Filter className="h-4 w-4 text-taupe" />
          <select value={level} onChange={e => setLevel(e.target.value)} className="rounded-md border border-input bg-background px-2 py-1">
            <option value="">Todos os níveis</option>
            {["Iniciante", "Intermediário", "Avançado"].map(n => <option key={n}>{n}</option>)}
          </select>
          <select value={cat} onChange={e => setCat(e.target.value)} className="rounded-md border border-input bg-background px-2 py-1">
            <option value="">Todas as categorias</option>
            {cats.map(c => <option key={c}>{c}</option>)}
          </select>
          <select value={tag} onChange={e => setTag(e.target.value)} className="rounded-md border border-input bg-background px-2 py-1">
            <option value="">Todas as tags</option>
            {tags.map(t => <option key={t}>#{t}</option>)}
          </select>
        </div>
      )}

      {docs.length === 0 ? (
        <div className="mt-16 rounded-2xl border border-dashed border-bege p-16 text-center">
          <p className="font-serif text-2xl">Parece que seus documentos também estão tentando se esconder.</p>
          <p className="mt-2 text-taupe">Que tal enviar o primeiro?</p>
          <Link to="/app"><Button className="mt-6 bg-vinho text-vinho-foreground hover:bg-vinho/90">Enviar meu primeiro texto</Button></Link>
        </div>
      ) : (
        <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map(d => (
            <DocCard key={d.id} doc={d}
              footer={
                <div className="flex flex-wrap gap-2 text-xs">
                  <button onClick={() => { setRenaming(d); setRenameValue(d.title); setRenameErr(null); }}
                    className="inline-flex items-center gap-1 rounded-md border border-input px-2 py-1 hover:bg-muted">
                    <Pencil className="h-3 w-3" /> Renomear
                  </button>
                  {d.status === "ERROR" && d.attempts < 3 && (
                    <button onClick={() => retry(d.id).then(load)}
                      className="inline-flex items-center gap-1 rounded-md border border-input px-2 py-1 hover:bg-muted">
                      <RefreshCw className="h-3 w-3" /> Tentar novamente
                    </button>
                  )}
                  {d.public && (
                    <button onClick={() => setPublic(d.id, false).then(load)}
                      className="inline-flex items-center gap-1 rounded-md border border-input px-2 py-1 hover:bg-muted">
                      <EyeOff className="h-3 w-3" /> Retirar do público
                    </button>
                  )}
                  <button onClick={() => setDeleting(d)}
                    className="ml-auto inline-flex items-center gap-1 rounded-md border border-vinho/40 px-2 py-1 text-vinho hover:bg-vinho/5">
                    <Trash2 className="h-3 w-3" /> Excluir
                  </button>
                </div>
              }
            />
          ))}
        </div>
      )}

      <Dialog open={!!renaming} onOpenChange={o => !o && setRenaming(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Renomear documento</DialogTitle>
            <DialogDescription>Escolha um novo título único.</DialogDescription>
          </DialogHeader>
          <Label htmlFor="rn">Novo título</Label>
          <Input id="rn" maxLength={150} value={renameValue} onChange={e => setRenameValue(e.target.value)}
            className={renameErr ? "border-vinho ring-1 ring-vinho" : ""} />
          {renameErr && <p className="text-sm text-vinho">{renameErr}</p>}
          <DialogFooter>
            <Button variant="outline" onClick={() => setRenaming(null)}>Cancelar</Button>
            <Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90"
              onClick={async () => {
                if (!renaming) return;
                try { await rename(renaming.id, renameValue); setRenaming(null); load(); }
                catch (e) { const er = e as Error & { status?: number }; setRenameErr(er.status === 409 ? "Título já existe." : er.message); }
              }}>Salvar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={!!deleting} onOpenChange={o => !o && setDeleting(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Excluir documento</DialogTitle>
            <DialogDescription>
              Esta ação é permanente. O documento será removido imediatamente e não poderá ser recuperado.
            </DialogDescription>
          </DialogHeader>
          <div className="rounded-lg border border-vinho/30 bg-vinho/5 p-3 text-sm">
            <strong>{deleting?.title}</strong>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleting(null)}>Cancelar</Button>
            <Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90"
              onClick={async () => { if (deleting) { await remove(deleting.id); setDeleting(null); load(); } }}>
              Excluir definitivamente
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
