import { createFileRoute, useRouter } from "@tanstack/react-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { CheckCircle2, FileText, Loader2, Upload } from "lucide-react";
import { PageHeader } from "@/components/PageHeader";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Progress } from "@/components/ui/progress";
import { Textarea } from "@/components/ui/textarea";
import { documentStatusLabel, levelLabel, moderationStatusLabel } from "@/lib/display";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { fieldError } from "@/services/api";
import { findDocumentById, sendDocument, type DocumentResponseDTO, type Visibility } from "@/services/documents.service";

export const Route = createFileRoute("/app/")({ component: NewDocumentPage });

const MIN_CONTENT = 20;
const MAX_CONTENT = 10_000;

function NewDocumentPage() {
  const { lang, t } = useI18n();
  const router = useRouter();
  const pollAttempts = useRef(0);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [visibility, setVisibility] = useState<Visibility>("PRIVATE");
  const [externalAiAllowed, setExternalAiAllowed] = useState(false);
  const [trainingUseAllowed, setTrainingUseAllowed] = useState(false);
  const [usageAccepted, setUsageAccepted] = useState(false);
  const [submitted, setSubmitted] = useState<DocumentResponseDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [fields, setFields] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [fileName, setFileName] = useState<string | null>(null);

  const formValid = useMemo(() => title.trim().length >= 3 && content.trim().length >= MIN_CONTENT && content.length <= MAX_CONTENT && usageAccepted && trainingUseAllowed, [title, content, usageAccepted, trainingUseAllowed]);

  const submittedDocumentId = submitted?.documentId;
  const submittedStatus = submitted?.status;

  useEffect(() => {
    if (!submittedDocumentId || !submittedStatus || !["PENDING", "PROCESSING"].includes(submittedStatus)) return;
    pollAttempts.current = 0;
    const timer = window.setInterval(async () => {
      pollAttempts.current += 1;
      try {
        const updated = await findDocumentById(submittedDocumentId);
        setSubmitted(updated);
        if (!["PENDING", "PROCESSING"].includes(updated.status) || pollAttempts.current >= 15) window.clearInterval(timer);
      } catch {
        if (pollAttempts.current >= 3) window.clearInterval(timer);
      }
    }, 2000);
    return () => window.clearInterval(timer);
  }, [submittedDocumentId, submittedStatus]);

  async function importFile(file?: File) {
    if (!file) return;
    if (!/\.(txt|md)$/i.test(file.name) && !file.type.startsWith("text/")) {
      setError(lang === "pt-BR" ? "Use um arquivo de texto (.txt ou .md)." : "Use a text file (.txt or .md)." );
      return;
    }
    if (file.size > 500_000) {
      setError(lang === "pt-BR" ? "O arquivo é muito grande para leitura local." : "The file is too large for local reading.");
      return;
    }
    const text = await file.text();
    setContent(text.slice(0, MAX_CONTENT));
    setFileName(file.name);
    if (!title.trim()) setTitle(file.name.replace(/\.[^.]+$/, "").slice(0, 150));
    setError(null);
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (!formValid) return;
    setError(null);
    setFields({});
    setSubmitting(true);
    try {
      const response = await sendDocument({ title: title.trim(), content, visibility, externalAiAllowed, trainingUseAllowed, usageTermsAccepted: usageAccepted });
      setSubmitted(response);
    } catch (currentError) {
      setFields({ title: fieldError(currentError, "title") || "", content: fieldError(currentError, "content") || "" });
      setError(friendlyError(currentError, t));
    } finally {
      setSubmitting(false);
    }
  }

  function reset() {
    setSubmitted(null); setTitle(""); setContent(""); setVisibility("PRIVATE"); setExternalAiAllowed(false); setTrainingUseAllowed(false); setUsageAccepted(false); setFileName(null); setError(null); setFields({});
  }

  if (submitted) {
    const processing = submitted.status === "PENDING" || submitted.status === "PROCESSING";
    const finished = submitted.status === "PROCESSED";
    return (
      <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 sm:py-10">
        <PageHeader eyebrow={t("new.eyebrow")} title={t("new.success.title")} description={t("new.success.subtitle")} />
        <section className="mt-8 rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7" aria-live="polite">
          <div className="flex items-start gap-3">
            {processing ? <Loader2 className="mt-1 h-6 w-6 animate-spin text-dourado" aria-hidden="true" /> : <CheckCircle2 className={`mt-1 h-6 w-6 ${finished ? "text-verde" : "text-destructive"}`} aria-hidden="true" />}
            <div><h2 className="font-serif text-2xl">{processing ? t("new.status.processing") : finished ? t("new.status.finished") : t("new.status.error")}</h2><p className="mt-1 text-sm text-muted-foreground">{submitted.title}</p></div>
          </div>
          {processing && <Progress value={submitted.status === "PROCESSING" ? 66 : 30} className="mt-6" />}
          <dl className="mt-7 grid gap-4 rounded-xl bg-muted/30 p-4 text-sm sm:grid-cols-2 lg:grid-cols-3">
            <Data label="ID" value={submitted.documentId} />
            <Data label="Status" value={documentStatusLabel(submitted.status, t)} />
            <Data label={lang === "pt-BR" ? "Visibilidade" : "Visibility"} value={submitted.visibility === "PUBLIC" ? t("common.public") : t("common.private")} />
            <Data label={lang === "pt-BR" ? "Moderação" : "Moderation"} value={moderationStatusLabel(submitted.moderationStatus, t)} />
            <Data label={lang === "pt-BR" ? "Categoria" : "Category"} value={submitted.analysis?.category ?? "—"} />
            <Data label={lang === "pt-BR" ? "Nível" : "Level"} value={submitted.analysis?.difficulty ? levelLabel(submitted.analysis.difficulty, t) : "—"} />
            <Data label={lang === "pt-BR" ? "Criado em" : "Created at"} value={new Date(submitted.createdAt).toLocaleString(lang)} />
          </dl>
          {submitted.analysis?.tags?.length ? <div className="mt-5 flex flex-wrap gap-2">{submitted.analysis.tags.map((tag) => <span key={tag} className="rounded-full bg-pessego/50 px-2.5 py-1 text-xs text-marrom">#{tag}</span>)}</div> : null}
          <div className="mt-7 flex flex-col gap-2 sm:flex-row">
            <Button onClick={() => void router.navigate({ to: "/app/library" })} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("new.goLibrary")}</Button>
            <Button variant="outline" onClick={reset}>{t("new.sendAnother")}</Button>
          </div>
        </section>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("new.eyebrow")} title={t("new.title")} description={t("new.subtitle")} />
      <form onSubmit={submit} className="mt-8 space-y-6" noValidate>
        <section className="space-y-5 rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7">
          <FormField id="document-title" label={t("new.field.title")} error={fields.title} required>
            <Input id="document-title" value={title} minLength={3} maxLength={150} onChange={(event) => setTitle(event.target.value)} aria-invalid={Boolean(fields.title) || undefined} aria-describedby={fields.title ? "document-title-error" : undefined} />
          </FormField>
          <div className="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-end">
            <FormField id="document-file" label={t("new.field.file")} hint={t("new.file.help")}>
              <Input id="document-file" type="file" accept=".txt,.md,text/plain,text/markdown" onChange={(event) => void importFile(event.target.files?.[0])} />
            </FormField>
            {fileName && <div className="flex items-center gap-2 rounded-lg border border-border bg-muted/30 px-3 py-2 text-xs text-muted-foreground"><FileText className="h-4 w-4" aria-hidden="true" />{fileName}</div>}
          </div>
          <FormField id="document-content" label={t("new.field.content")} error={fields.content} required>
            <Textarea id="document-content" rows={14} minLength={MIN_CONTENT} maxLength={MAX_CONTENT} value={content} onChange={(event) => setContent(event.target.value)} placeholder={t("new.content.placeholder")} aria-invalid={Boolean(fields.content) || undefined} aria-describedby={fields.content ? "document-content-error document-content-count" : "document-content-count"} />
          </FormField>
          <p id="document-content-count" className="-mt-3 text-right text-xs text-muted-foreground">{content.length.toLocaleString(lang)}/{MAX_CONTENT.toLocaleString(lang)}</p>
        </section>

        <section className="space-y-5 rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7">
          <div>
            <Label htmlFor="visibility">{t("new.visibility")}</Label>
            <p className="mt-1 text-xs text-muted-foreground">{t("new.visibility.help")}</p>
            <select id="visibility" value={visibility} onChange={(event) => setVisibility(event.target.value as Visibility)} className="mt-2 min-h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
              <option value="PRIVATE">{t("common.private")}</option><option value="PUBLIC">{t("common.public")}</option>
            </select>
          </div>
          <Consent checked={externalAiAllowed} onChange={setExternalAiAllowed} label={t("new.externalAi")} />
          <Consent checked={trainingUseAllowed} onChange={setTrainingUseAllowed} label={t("new.training")} hint={t("new.training.help")} required />
          <Consent checked={usageAccepted} onChange={setUsageAccepted} label={lang === "pt-BR" ? "Confirmo que tenho autorização para armazenar este conteúdo e aceito os Termos e a Política de Privacidade." : "I confirm that I am authorized to store this content and accept the Terms and Privacy Policy."} required />
        </section>

        {error && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}
        <div className="flex justify-end"><LoadingButton type="submit" loading={submitting} loadingLabel={t("new.submitting")} disabled={!formValid} size="lg" className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90 sm:w-auto"><Upload className="mr-2 h-4 w-4" aria-hidden="true" />{t("new.submit")}</LoadingButton></div>
      </form>
    </div>
  );
}

function Consent({ checked, onChange, label, hint, required }: { checked: boolean; onChange: (checked: boolean) => void; label: string; hint?: string; required?: boolean }) {
  return (
    <label className="flex cursor-pointer items-start gap-3 rounded-xl border border-border p-4 transition hover:bg-muted/30">
      <Checkbox checked={checked} onCheckedChange={(value) => onChange(Boolean(value))} required={required} />
      <span className="text-sm leading-relaxed"><span>{label}{required ? " *" : ""}</span>{hint && <span className="mt-1 block text-xs text-muted-foreground">{hint}</span>}</span>
    </label>
  );
}

function Data({ label, value }: { label: string; value: React.ReactNode }) {
  return <div><dt className="text-xs font-medium uppercase tracking-wider text-muted-foreground">{label}</dt><dd className="mt-1 break-words font-medium">{value}</dd></div>;
}
