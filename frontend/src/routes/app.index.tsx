import { createFileRoute, useRouter } from "@tanstack/react-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { BookOpen, CheckCircle2 } from "lucide-react";
import { PageHeader } from "@/components/PageHeader";
import { FormField } from "@/components/FormField";
import { LoadingButton } from "@/components/LoadingButton";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { documentStatusLabel, levelLabel, moderationStatusLabel } from "@/lib/display";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { fieldError } from "@/services/api";
import { findDocumentById, sendDocument, type DocumentResponseDTO, type Visibility } from "@/services/documents.service";

export const Route = createFileRoute("/app/")({ component: NewDocumentPage });

const MIN_CONTENT = 20;
const MAX_CONTENT = 10_000;
type ProcessingStage = "submitting" | "pending" | "processing";

function NewDocumentPage() {
  const { lang, t } = useI18n();
  const router = useRouter();
  const pollAttempts = useRef(0);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [visibility, setVisibility] = useState<Visibility>("PRIVATE");
  const [trainingUseAllowed, setTrainingUseAllowed] = useState(false);
  const [usageAccepted, setUsageAccepted] = useState(false);
  const [submitted, setSubmitted] = useState<DocumentResponseDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [fields, setFields] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  const formValid = useMemo(
    () =>
      title.trim().length >= 3 &&
      content.trim().length >= MIN_CONTENT &&
      content.length <= MAX_CONTENT &&
      usageAccepted &&
      trainingUseAllowed,
    [title, content, usageAccepted, trainingUseAllowed],
  );

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

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (!formValid) return;
    setError(null);
    setFields({});
    setSubmitting(true);
    try {
      const response = await sendDocument({
        title: title.trim(),
        content,
        visibility,
        externalAiAllowed: true,
        trainingUseAllowed,
        usageTermsAccepted: usageAccepted,
      });
      setSubmitted(response);
    } catch (currentError) {
      setFields({
        title: fieldError(currentError, "title") || "",
        content: fieldError(currentError, "content") || "",
      });
      setError(friendlyError(currentError, t));
    } finally {
      setSubmitting(false);
    }
  }

  function reset() {
    setSubmitted(null);
    setTitle("");
    setContent("");
    setVisibility("PRIVATE");
    setTrainingUseAllowed(false);
    setUsageAccepted(false);
    setError(null);
    setFields({});
  }

  if (submitting && !submitted) {
    return <ProcessingScreen stage="submitting" />;
  }

  if (submitted) {
    const processing = submitted.status === "PENDING" || submitted.status === "PROCESSING";
    const finished = submitted.status === "PROCESSED";

    if (processing) {
      return <ProcessingScreen stage={submitted.status === "PENDING" ? "pending" : "processing"} />;
    }

    return (
      <div className="mx-auto w-full px-4 py-8 sm:px-6 sm:py-10 lg:w-[75vw] lg:max-w-[90rem]">
        <PageHeader eyebrow={t("new.eyebrow")} title={t("new.success.title")} description={t("new.success.subtitle")} />
        <section className="mt-8 rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7" aria-live="polite">
          <div className="flex items-start gap-3">
            <CheckCircle2
              className={`mt-1 h-6 w-6 ${finished ? "text-verde" : "text-destructive"}`}
              aria-hidden="true"
            />
            <div>
              <h2 className="font-serif text-2xl">{finished ? t("new.status.finished") : t("new.status.error")}</h2>
              <p className="mt-1 text-sm text-muted-foreground">{submitted.title}</p>
            </div>
          </div>
          <dl className="mt-7 grid gap-4 rounded-xl bg-muted/30 p-4 text-sm sm:grid-cols-2 lg:grid-cols-3">
            <Data label="Status" value={documentStatusLabel(submitted.status, t)} />
            <Data label={lang === "pt-BR" ? "Visibilidade" : "Visibility"} value={submitted.visibility === "PUBLIC" ? t("common.public") : t("common.private")} />
            <Data label={lang === "pt-BR" ? "Moderação" : "Moderation"} value={moderationStatusLabel(submitted.moderationStatus, t)} />
            <Data label={lang === "pt-BR" ? "Categoria" : "Category"} value={submitted.analysis?.category ?? "—"} />
            <Data label={lang === "pt-BR" ? "Nível" : "Level"} value={submitted.analysis?.difficulty ? levelLabel(submitted.analysis.difficulty, t) : "—"} />
            <Data label={lang === "pt-BR" ? "Criado em" : "Created at"} value={new Date(submitted.createdAt).toLocaleString(lang)} />
          </dl>
          {submitted.analysis?.tags?.length ? (
            <div className="mt-5 flex flex-wrap gap-2">
              {submitted.analysis.tags.map((tag) => (
                <span key={tag} className="rounded-full bg-pessego/50 px-2.5 py-1 text-xs text-marrom">
                  #{tag}
                </span>
              ))}
            </div>
          ) : null}
          <div className="mt-7 flex flex-col gap-2 sm:flex-row">
            <Button onClick={() => void router.navigate({ to: "/app/library" })} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">
              {t("new.goLibrary")}
            </Button>
            <Button variant="outline" onClick={reset}>
              {t("new.sendAnother")}
            </Button>
          </div>
        </section>
      </div>
    );
  }

  return (
    <div className="mx-auto w-full px-4 py-8 sm:px-6 sm:py-10 lg:w-[75vw] lg:max-w-[90rem]">
      <PageHeader eyebrow={t("new.eyebrow")} title={t("new.title")} description={t("new.subtitle")} />
      <form onSubmit={submit} className="mt-8 space-y-6" noValidate>
        <section className="space-y-5 rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7">
          <FormField id="document-title" label={t("new.field.title")} error={fields.title} required>
            <Input
              id="document-title"
              value={title}
              minLength={3}
              maxLength={150}
              onChange={(event) => setTitle(event.target.value)}
              aria-invalid={Boolean(fields.title) || undefined}
              aria-describedby={fields.title ? "document-title-error" : undefined}
            />
          </FormField>
          <FormField id="document-content" label={t("new.field.content")} error={fields.content} required>
            <Textarea
              id="document-content"
              rows={14}
              className="min-h-80 resize-y"
              minLength={MIN_CONTENT}
              maxLength={MAX_CONTENT}
              value={content}
              onChange={(event) => setContent(event.target.value)}
              placeholder={t("new.content.placeholder")}
              aria-invalid={Boolean(fields.content) || undefined}
              aria-describedby={fields.content ? "document-content-error document-content-count" : "document-content-count"}
            />
          </FormField>
          <p id="document-content-count" className="-mt-3 text-right text-xs text-muted-foreground">
            {content.length.toLocaleString(lang)}/{MAX_CONTENT.toLocaleString(lang)}
          </p>
        </section>

        <section className="space-y-5 rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7">
          <div>
            <Label htmlFor="visibility">{t("new.visibility")}</Label>
            <p className="mt-1 text-xs text-muted-foreground">{t("new.visibility.help")}</p>
            <select
              id="visibility"
              value={visibility}
              onChange={(event) => setVisibility(event.target.value as Visibility)}
              className="mt-2 min-h-10 w-full rounded-md border border-input bg-field px-3 dark:bg-background py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <option value="PRIVATE">{t("common.private")}</option>
              <option value="PUBLIC">{t("common.public")}</option>
            </select>
          </div>
          <Consent checked={trainingUseAllowed} onChange={setTrainingUseAllowed} label={t("new.training")} hint={t("new.training.help")} required />
          <Consent
            checked={usageAccepted}
            onChange={setUsageAccepted}
            label={
              lang === "pt-BR"
                ? "Confirmo que tenho autorização para armazenar este conteúdo e aceito os Termos e a Política de Privacidade."
                : "I confirm that I am authorized to store this content and accept the Terms and Privacy Policy."
            }
            required
          />
        </section>

        {error && (
          <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">
            {error}
          </p>
        )}
        <div className="flex justify-end">
          <LoadingButton
            type="submit"
            loading={submitting}
            loadingLabel={t("new.submitting")}
            disabled={!formValid}
            size="lg"
            className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90 sm:w-auto"
          >
            <BookOpen className="mr-2 h-4 w-4" aria-hidden="true" />
            {t("new.submit")}
          </LoadingButton>
        </div>
      </form>
    </div>
  );
}

function ProcessingScreen({ stage }: { stage: ProcessingStage }) {
  const { t } = useI18n();
  const stageMessages: Record<ProcessingStage, string> = {
    submitting: t("new.processing.submitting"),
    pending: t("new.processing.pending"),
    processing: t("new.processing.processing"),
  };
  const order: ProcessingStage[] = ["submitting", "pending", "processing"];
  const currentIndex = order.indexOf(stage);

  return (
    <div className="mx-auto flex min-h-[75vh] w-full flex-col px-4 py-7 sm:px-6 sm:py-9 lg:w-[75vw] lg:max-w-[90rem]" aria-live="polite">
      <div className="flex items-center gap-4" aria-hidden="true">
        <span className="h-px flex-1 bg-gradient-to-r from-transparent via-dourado/40 to-dourado/20" />
        <span className="font-serif text-lg text-dourado">✦</span>
        <span className="h-px flex-1 bg-gradient-to-l from-transparent via-dourado/40 to-dourado/20" />
      </div>

      <div className="flex flex-1 items-center justify-center py-12">
        <div className="flex flex-col items-center space-y-6 text-center" role="status" aria-label={stageMessages[stage]}>
          <div className="relative h-20 w-20">
            <div className="absolute inset-0 rounded-full border-4 border-vinho/20" />
            <div className="absolute inset-0 animate-spin rounded-full border-4 border-transparent border-t-vinho" />
            <div className="absolute inset-2 flex items-center justify-center rounded-full bg-vinho/5">
              <BookOpen className="h-6 w-6 animate-pulse text-vinho" aria-hidden="true" />
            </div>
          </div>
          <div className="space-y-2">
            <p className="font-serif text-xl font-semibold text-foreground">{stageMessages[stage]}</p>
            <p className="max-w-xs text-sm leading-relaxed text-muted-foreground">{t("new.processing.description")}</p>
          </div>
          <div className="flex gap-2" aria-hidden="true">
            {order.map((item, index) => (
              <div
                key={item}
                className={`h-1.5 rounded-full transition-all duration-300 ${index <= currentIndex ? "w-6 bg-vinho" : "w-1.5 bg-border"}`}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function Consent({ checked, onChange, label, hint, required }: { checked: boolean; onChange: (checked: boolean) => void; label: string; hint?: string; required?: boolean }) {
  return (
    <label className="flex cursor-pointer items-start gap-3 rounded-xl border border-border p-4 transition hover:bg-muted/30">
      <Checkbox checked={checked} onCheckedChange={(value) => onChange(Boolean(value))} required={required} />
      <span className="text-sm leading-relaxed">
        <span>
          {label}
          {required ? " *" : ""}
        </span>
        {hint && <span className="mt-1 block text-xs text-muted-foreground">{hint}</span>}
      </span>
    </label>
  );
}

function Data({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-wider text-muted-foreground">{label}</dt>
      <dd className="mt-1 break-words font-medium">{value}</dd>
    </div>
  );
}
