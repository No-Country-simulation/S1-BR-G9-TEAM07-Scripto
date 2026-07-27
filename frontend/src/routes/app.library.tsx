import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { Filter, RefreshCw, Trash2, Pencil, EyeOff } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { DocCard } from "@/components/common/DocCard";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { listMine, remove, rename, requestSummary, retry, setPublic, type Doc } from "@/services/documents.service";

export const Route = createFileRoute("/app/library")({ component: LibraryPage });

function LibraryPage() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const [level, setLevel] = useState("");
  const [cat, setCat] = useState("");
  const [tag, setTag] = useState("");
  const [selected, setSelected] = useState<Doc | null>(null);
  const [summaryTarget, setSummaryTarget] = useState<Doc | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summaryMessage, setSummaryMessage] = useState<string | null>(null);
  const [renaming, setRenaming] = useState<Doc | null>(null);
  const [renameValue, setRenameValue] = useState("");
  const [renameErr, setRenameErr] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<Doc | null>(null);

  const load = () => listMine().then(setDocs);
  useEffect(() => {
    load();
    const interval = window.setInterval(load, 1500);
    return () => window.clearInterval(interval);
  }, []);

  const filtered = useMemo(
    () => docs.filter((doc) => (!level || doc.level === level) && (!cat || doc.category === cat) && (!tag || doc.tags.includes(tag))),
    [docs, level, cat, tag],
  );
  const cats = [...new Set(docs.map((doc) => doc.category).filter((category) => category && category !== "—"))];
  const tags = [...new Set(docs.flatMap((doc) => doc.tags))];

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

      {summaryMessage && <p role="alert" className="mt-5 rounded-lg border border-bege bg-muted/40 p-3 text-sm">{summaryMessage}</p>}

      {docs.length > 0 && (
        <div className="mt-6 flex flex-wrap items-center gap-3 rounded-xl border border-bege bg-muted/30 p-3 text-sm">
          <Filter className="h-4 w-4" />
          <select value={level} onChange={(event) => setLevel(event.target.value)} className="rounded-md border bg-background px-2 py-1">
            <option value="">Todos os níveis</option>
            <option>Iniciante</option><option>Intermediário</option><option>Avançado</option>
          </select>
          <select value={cat} onChange={(event) => setCat(event.target.value)} className="rounded-md border bg-background px-2 py-1">
            <option value="">Todas as categorias</option>
            {cats.map((category) => <option key={category}>{category}</option>)}
          </select>
          <select value={tag} onChange={(event) => setTag(event.target.value)} className="rounded-md border bg-background px-2 py-1">
            <option value="">Todas as tags</option>
            {tags.map((item) => <option key={item}>{item}</option>)}
          </select>
        </div>
      )}

      {docs.length === 0 ? (
        <div className="mt-16 rounded-xl border border-dashed border-bege p-10 text-center">
          <p className="font-serif text-2xl">Parece que seus documentos também estão tentando se esconder.</p>
          <p className="mt-2 text-taupe">Que tal enviar o primeiro?</p>
          <Link to="/app"><Button className="mt-6 bg-vinho text-vinho-foreground hover:bg-vinho/90">Enviar meu primeiro texto</Button></Link>
        </div>
      ) : (
        <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((doc) => (
            <DocCard
              key={doc.id}
              doc={doc}
              onClick={() => setSelected(doc)}
              onRequestSummary={() => { setSummaryMessage(null); setSummaryTarget(doc); }}
              footer={
                <div className="flex flex-wrap gap-2 text-xs">
                  <button onClick={() => { setRenaming(doc); setRenameValue(doc.title); setRenameErr(null); }} className="inline-flex items-center gap-1 rounded-md border border-input px-2 py-1 hover:bg-muted">
                    <Pencil className="h-3 w-3" /> Renomear
                  </button>
                  {doc.status === "ERROR" && doc.attempts < 3 && (
                    <button onClick={() => retry(doc.id).then(load)} className="inline-flex items-center gap-1 rounded-md border border-input px-2 py-1 hover:bg-muted">
                      <RefreshCw className="h-3 w-3" /> Tentar novamente
                    </button>
                  )}
                  {doc.public && (
                    <button onClick={() => setPublic(doc.id, false).then(load)} className="inline-flex items-center gap-1 rounded-md border border-input px-2 py-1 hover:bg-muted">
                      <EyeOff className="h-3 w-3" /> Retirar do público
                    </button>
                  )}
                  <button onClick={() => setDeleting(doc)} className="ml-auto inline-flex items-center gap-1 rounded-md border border-vinho/40 px-2 py-1 text-vinho hover:bg-vinho/5">
                    <Trash2 className="h-3 w-3" /> Excluir
                  </button>
                </div>
              }
            />
          ))}
        </div>
      )}

      <Dialog open={!!selected} onOpenChange={(open) => !open && setSelected(null)}>
        <DialogContent className="max-h-[85vh] max-w-3xl overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{selected?.title}</DialogTitle>
            <DialogDescription>Conteúdo original e informações do documento.</DialogDescription>
          </DialogHeader>
          {selected && (
            <div className="space-y-4">
              <div className="flex flex-wrap gap-2 text-xs">
                <span className="rounded-md border px-2 py-1">{selected.category}</span>
                <span className="rounded-md border px-2 py-1">{selected.level}</span>
                <span className="rounded-md border px-2 py-1">{selected.status}</span>
                <span className="rounded-md border px-2 py-1">{new Date(selected.createdAt).toLocaleDateString("pt-BR")}</span>
              </div>
              <section>
                <h3 className="font-medium">Resumo</h3>
                <p className="mt-1 text-sm text-muted-foreground">{selected.summary}</p>
              </section>
              <section>
                <h3 className="font-medium">Conteúdo original</h3>
                {selected.source === "file" && !selected.content ? (
                  <p className="mt-1 text-sm text-muted-foreground">O conteúdo do arquivo não está armazenado no frontend. Arquivo enviado: {selected.fileName ?? "não informado"}.</p>
                ) : (
                  <pre className="mt-2 whitespace-pre-wrap break-words rounded-lg border bg-muted/30 p-4 font-sans text-sm">{selected.content || "Conteúdo não disponível."}</pre>
                )}
              </section>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={!!summaryTarget} onOpenChange={(open) => !open && setSummaryTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Gerar resumo</DialogTitle>
            <DialogDescription>A geração de resumo possui um limite de 3 tentativas diárias por usuário. Deseja continuar?</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" disabled={summaryLoading} onClick={() => setSummaryTarget(null)}>Cancelar</Button>
            <Button
              disabled={summaryLoading}
              className="bg-vinho text-vinho-foreground hover:bg-vinho/90"
              onClick={async () => {
                if (!summaryTarget) return;
                setSummaryLoading(true);
                try {
                  await requestSummary(summaryTarget.id);
                } catch (error) {
                  setSummaryMessage((error as Error).message);
                } finally {
                  setSummaryLoading(false);
                  setSummaryTarget(null);
                }
              }}
            >
              {summaryLoading ? "Solicitando…" : "Continuar"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={!!renaming} onOpenChange={(open) => !open && setRenaming(null)}>
        <DialogContent>
          <DialogHeader><DialogTitle>Renomear documento</DialogTitle><DialogDescription>Escolha um novo título único.</DialogDescription></DialogHeader>
          <Label htmlFor="rn">Novo título</Label>
          <Input id="rn" maxLength={150} value={renameValue} onChange={(event) => setRenameValue(event.target.value)} className={renameErr ? "border-vinho ring-1 ring-vinho" : ""} />
          {renameErr && <p className="text-sm text-vinho">{renameErr}</p>}
          <DialogFooter>
            <Button variant="outline" onClick={() => setRenaming(null)}>Cancelar</Button>
            <Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90" onClick={async () => {
              if (!renaming) return;
              try { await rename(renaming.id, renameValue); setRenaming(null); load(); }
              catch (error) { const current = error as Error & { status?: number }; setRenameErr(current.status === 409 ? "Título já existe." : current.message); }
            }}>Salvar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={!!deleting} onOpenChange={(open) => !open && setDeleting(null)}>
        <DialogContent>
          <DialogHeader><DialogTitle>Excluir documento</DialogTitle><DialogDescription>Esta ação é permanente e não poderá ser desfeita.</DialogDescription></DialogHeader>
          <div className="rounded-lg border border-vinho/30 bg-vinho/5 p-3 text-sm"><strong>{deleting?.title}</strong></div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleting(null)}>Cancelar</Button>
            <Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90" onClick={async () => { if (deleting) { await remove(deleting.id); setDeleting(null); load(); } }}>Excluir definitivamente</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
