import { createFileRoute, useRouter } from "@tanstack/react-router";
import { useRef, useState } from "react";
import { FileText, UploadCloud, Loader2, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import { create, type Doc } from "@/services/documents.service";

export const Route = createFileRoute("/app/")({ component: NewDoc });

const MAX_TEXT = 100_000;
const MAX_FILE_MB = 10;

function NewDoc() {
  const router = useRouter();
  const [mode, setMode] = useState<"paste" | "file">("paste");
  const [title, setTitle] = useState(""); const [content, setContent] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [consent, setConsent] = useState(false);
  const [titleErr, setTitleErr] = useState<string | null>(null);
  const [contentErr, setContentErr] = useState<string | null>(null);
  const contentRef = useRef<HTMLTextAreaElement>(null);
  const [err, setErr] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState<Doc | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const locked = !!submitted;

  async function submit(e: React.FormEvent) {
    e.preventDefault(); setErr(null); setTitleErr(null); setContentErr(null);
    if (!title.trim()) return setTitleErr("O título do documento é obrigatório.");
    if (mode === "paste" && !content.trim()) {
      setContentErr("O conteúdo do documento é obrigatório.");
      requestAnimationFrame(() => contentRef.current?.focus());
      return;
    }
    if (!consent) return setErr("Você precisa confirmar o consentimento de publicação.");
    if (title.length > 150) return setTitleErr("Título muito longo (máx 150).");
    if (mode === "paste" && content.length > MAX_TEXT) return setErr(`Texto acima de ${MAX_TEXT.toLocaleString()} caracteres.`);
    if (mode === "file" && (!file || file.size > MAX_FILE_MB * 1024 * 1024))
      return setErr(`Arquivo obrigatório (até ${MAX_FILE_MB}MB).`);

    setSubmitting(true);
    try {
      const doc = await create({ title, content, fileName: file?.name, source: mode });
      setSubmitted(doc);
    } catch (e) {
      const anyErr = e as Error & { status?: number };
      if (anyErr.status === 409) setTitleErr("Já existe um documento com este título. Escolha outro.");
      else setErr(anyErr.message);
    } finally { setSubmitting(false); }
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-10">
      <header>
        <p className="text-xs uppercase tracking-widest text-dourado">Novo</p>
        <h1 className="font-serif text-4xl">Novo documento</h1>
        <p className="mt-1 text-sm text-taupe">Cole um texto ou envie um arquivo — o Scripto cuida do resto.</p>
        <OrnamentDivider className="mt-6" />
      </header>

      <div className="mt-6 grid grid-cols-2 gap-2 rounded-xl bg-muted p-1">
        {(["paste", "file"] as const).map(m => (
          <button
            key={m} disabled={locked} onClick={() => setMode(m)}
            className={`flex items-center justify-center gap-2 rounded-lg py-2 text-sm transition ${
              mode === m ? "bg-background shadow-sm" : "text-taupe"
            }`}
          >
            {m === "paste" ? <FileText className="h-4 w-4" /> : <UploadCloud className="h-4 w-4" />}
            {m === "paste" ? "Colar texto" : "Enviar arquivo"}
          </button>
        ))}
      </div>

      <form onSubmit={submit} className="mt-6 space-y-5">
        <div>
          <Label htmlFor="title">Título *</Label>
          <Input id="title" maxLength={150} required disabled={locked}
            value={title} onChange={e => setTitle(e.target.value)}
            aria-invalid={!!titleErr}
            className={`mt-1 ${titleErr ? "border-vinho ring-1 ring-vinho" : ""}`} />
          <div className="mt-1 flex justify-between text-xs text-taupe">
            <span>{titleErr && <span className="text-vinho">{titleErr}</span>}</span>
            <span>{title.length}/150</span>
          </div>
        </div>

        {mode === "paste" ? (
          <div>
            <Label htmlFor="content">Conteúdo *</Label>
            <Textarea ref={contentRef} id="content" rows={12} required disabled={locked} value={content}
              onChange={e => { setContent(e.target.value); if (contentErr) setContentErr(null); }}
              aria-invalid={!!contentErr}
              aria-describedby={contentErr ? "content-error" : undefined}
              placeholder="Cole aqui o texto que deseja organizar…"
              className={`mt-1 font-mono text-sm ${contentErr ? "border-vinho ring-1 ring-vinho" : ""}`} />
            <div className="mt-1 flex justify-between gap-3 text-xs">
              <span id="content-error" role="alert" className="text-vinho">{contentErr}</span>
              <span className="ml-auto text-taupe">
              {content.length.toLocaleString()} / {MAX_TEXT.toLocaleString()} caracteres</span>
            </div>
          </div>
        ) : (
          <div>
            <Label htmlFor="file">Arquivo (PDF, DOCX, TXT, MD — até 10MB)</Label>
            <Input id="file" type="file" accept=".pdf,.docx,.txt,.md" disabled={locked}
              onChange={e => setFile(e.target.files?.[0] ?? null)} className="mt-1" />
            {file && <p className="mt-1 text-xs text-taupe">{file.name}</p>}
          </div>
        )}

        <label className="flex items-start gap-2 text-sm text-taupe">
          <Checkbox checked={consent} onCheckedChange={v => setConsent(!!v)} disabled={locked} />
          <span>
            Estou ciente de que este conteúdo ficará disponível na biblioteca pública do Scripto e poderá ser
            recomendado a outros estudantes.
          </span>
        </label>

        {err && <p role="alert" className="text-sm text-vinho">{err}</p>}

        {!locked ? (
          <Button type="submit" disabled={submitting} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">
            {submitting ? "Enviando…" : "Enviar para análise"}
          </Button>
        ) : (
          <div className="rounded-xl border border-verde/30 bg-verde/5 p-5">
            <div className="flex items-center gap-2 text-verde">
              {submitted.status === "PROCESSED" ? <CheckCircle2 className="h-5 w-5" /> : <Loader2 className="h-5 w-5 animate-spin" />}
              <p className="font-medium">
                Documento em análise · status: {submitted.status}
              </p>
            </div>
            <p className="mt-2 text-sm text-taupe">
              Você pode acompanhar o processamento na sua biblioteca. Não precisa esperar aqui.
            </p>
            <div className="mt-4 flex gap-2">
              <Button onClick={() => router.navigate({ to: "/app/library" })}
                className="bg-vinho text-vinho-foreground hover:bg-vinho/90">
                Ir para minha biblioteca
              </Button>
              <Button variant="outline" onClick={() => {
                setSubmitted(null); setTitle(""); setContent(""); setFile(null); setConsent(false);
              }}>Enviar outro</Button>
            </div>
          </div>
        )}
      </form>
    </div>
  );
}
