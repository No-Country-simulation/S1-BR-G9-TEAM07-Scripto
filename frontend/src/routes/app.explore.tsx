import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { Filter, Flag } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { DocCard } from "@/components/common/DocCard";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import {
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle,
} from "@/components/ui/dialog";
import { listPublic, recommendations, type Doc } from "@/services/documents.service";
import { createReport } from "@/services/admin.service";

export const Route = createFileRoute("/app/explore")({ component: ExplorePage });

const REASONS = ["Conteúdo inadequado", "Violação de direitos autorais", "Spam ou propaganda", "Informação falsa", "Outro"];

function ExplorePage() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const [recs, setRecs] = useState<Doc[]>([]);
  const [level, setLevel] = useState(""); const [cat, setCat] = useState(""); const [tag, setTag] = useState("");
  const [open, setOpen] = useState<Doc | null>(null);
  const [report, setReport] = useState<Doc | null>(null);
  const [reason, setReason] = useState(REASONS[0]); const [note, setNote] = useState(""); const [sent, setSent] = useState(false);

  useEffect(() => { listPublic().then(setDocs); recommendations().then(setRecs); }, []);

  const filtered = useMemo(() => docs.filter(d =>
    (!level || d.level === level) && (!cat || d.category === cat) && (!tag || d.tags.includes(tag))
  ), [docs, level, cat, tag]);

  const cats = [...new Set(docs.map(d => d.category))];
  const tags = [...new Set(docs.flatMap(d => d.tags))];

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <header>
        <p className="text-xs uppercase tracking-widest text-dourado">Comunidade</p>
        <h1 className="font-serif text-4xl">Explorar biblioteca pública</h1>
        <p className="mt-1 text-sm text-taupe">Descubra o que outros estudantes andam lendo.</p>
        <OrnamentDivider className="mt-6" />
      </header>

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
        <p className="mt-16 rounded-2xl border border-dashed border-bege p-12 text-center text-taupe">
          Ainda não há textos públicos por aqui. Seja o primeiro a compartilhar!
        </p>
      ) : (
        <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map(d => (
            <DocCard key={d.id} doc={d} showAuthor onClick={() => setOpen(d)}
              footer={
                <button onClick={() => { setReport(d); setSent(false); setNote(""); }}
                  className="inline-flex items-center gap-1 text-xs text-vinho hover:underline">
                  <Flag className="h-3 w-3" /> Denunciar
                </button>
              } />
          ))}
        </div>
      )}

      {recs.length > 0 && (
        <section className="mt-16">
          <h2 className="font-serif text-2xl">Recomendações para você</h2>
          <p className="text-sm text-taupe">Baseado nas tags e níveis dos seus documentos.</p>
          <OrnamentDivider className="mt-4" />
          <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {recs.map(d => <DocCard key={d.id} doc={d} showAuthor onClick={() => setOpen(d)} />)}
          </div>
        </section>
      )}

      <Dialog open={!!open} onOpenChange={o => !o && setOpen(null)}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="font-serif text-2xl">{open?.title}</DialogTitle>
            <DialogDescription>por {open?.ownerName} · {open?.category} · {open?.level}</DialogDescription>
          </DialogHeader>
          <p className="text-sm text-foreground">{open?.summary}</p>
          <div className="flex flex-wrap gap-1.5">
            {open?.tags.map(t => <span key={t} className="rounded-full bg-pessego/50 px-2 py-0.5 text-xs">#{t}</span>)}
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={!!report} onOpenChange={o => !o && setReport(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Denunciar documento</DialogTitle>
            <DialogDescription>Sua denúncia será revisada pela moderação.</DialogDescription>
          </DialogHeader>
          {sent ? (
            <p className="rounded-lg bg-verde/10 p-4 text-sm text-verde">Denúncia enviada. Obrigado!</p>
          ) : (
            <>
              <Label htmlFor="reason">Motivo</Label>
              <select id="reason" value={reason} onChange={e => setReason(e.target.value)}
                className="rounded-md border border-input bg-background px-2 py-2 text-sm">
                {REASONS.map(r => <option key={r}>{r}</option>)}
              </select>
              <Label htmlFor="note">Observação (opcional)</Label>
              <Textarea id="note" rows={3} value={note} onChange={e => setNote(e.target.value)} />
              <DialogFooter>
                <Button variant="outline" onClick={() => setReport(null)}>Cancelar</Button>
                <Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90"
                  onClick={async () => { if (report) { await createReport({ docId: report.id, docTitle: report.title, reason, note }); setSent(true); } }}>
                  Enviar denúncia
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}