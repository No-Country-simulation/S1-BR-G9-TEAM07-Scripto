import { motion } from "framer-motion";
import { AcanthusCorner } from "@/components/ornaments/Acanthus";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { CheckCircle2, Clock, Loader2, AlertTriangle, User as UserIcon, Sparkles } from "lucide-react";
import type { Doc, DocStatus } from "@/services/documents.service";

const STATUS: Record<DocStatus, { label: string; className: string; Icon: typeof Clock }> = {
  PENDING: { label: "Pendente", className: "bg-muted text-muted-foreground", Icon: Clock },
  PROCESSING: { label: "Processando", className: "bg-pessego text-marrom", Icon: Loader2 },
  PROCESSED: { label: "Concluído", className: "bg-verde text-verde-foreground", Icon: CheckCircle2 },
  ERROR: { label: "Erro", className: "bg-vinho text-vinho-foreground", Icon: AlertTriangle },
};

export function DocCard({
  doc,
  onClick,
  onRequestSummary,
  footer,
  showAuthor,
}: {
  doc: Doc;
  onClick?: () => void;
  onRequestSummary?: () => void;
  footer?: React.ReactNode;
  showAuthor?: boolean;
}) {
  const s = STATUS[doc.status];

  function openWithKeyboard(event: React.KeyboardEvent<HTMLElement>) {
    if (!onClick || (event.key !== "Enter" && event.key !== " ")) return;
    event.preventDefault();
    onClick();
  }

  return (
    <motion.article
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      role={onClick ? "button" : undefined}
      tabIndex={onClick ? 0 : undefined}
      onClick={onClick}
      onKeyDown={openWithKeyboard}
      className={`relative overflow-hidden rounded-xl border border-bege bg-card p-5 shadow-[0_1px_2px_rgba(31,23,8,0.04)] transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-vinho ${
        onClick ? "cursor-pointer hover:-translate-y-0.5 hover:shadow-[0_4px_16px_rgba(31,23,8,0.08)]" : ""
      }`}
      aria-label={onClick ? `Abrir documento ${doc.title}` : undefined}
    >
      <AcanthusCorner className="pointer-events-none absolute -right-2 -top-2 h-14 w-14 text-dourado/40" />
      <div className="flex items-start justify-between gap-3">
        <h3 className="font-serif text-lg leading-tight text-foreground">{doc.title}</h3>
        <Badge className={`gap-1 whitespace-nowrap ${s.className}`}>
          <s.Icon className={`h-3 w-3 ${doc.status === "PROCESSING" ? "animate-spin" : ""}`} aria-hidden="true" />
          {s.label}
        </Badge>
      </div>
      {showAuthor && (
        <p className="mt-1 flex items-center gap-1 text-xs text-taupe">
          <UserIcon className="h-3 w-3" aria-hidden="true" /> {doc.ownerName}
        </p>
      )}
      <div className="mt-3 flex items-start gap-2">
        <p className="line-clamp-2 flex-1 text-sm text-muted-foreground">{doc.summary}</p>
        {onRequestSummary && (
          <Button
            type="button"
            size="icon"
            variant="ghost"
            aria-label={`Gerar resumo de ${doc.title}`}
            title="Gerar resumo"
            onClick={(event) => {
              event.stopPropagation();
              onRequestSummary();
            }}
          >
            <Sparkles className="h-4 w-4" />
          </Button>
        )}
      </div>
      <div className="mt-4 flex flex-wrap items-center gap-2">
        {doc.category !== "—" && <span className="rounded-md bg-muted px-2 py-0.5 text-xs">{doc.category}</span>}
        <span className="rounded-md border border-bege px-2 py-0.5 text-xs text-taupe">{doc.level}</span>
      </div>
      {doc.tags.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {doc.tags.map((tag) => (
            <span key={tag} className="rounded-full bg-pessego/50 px-2 py-0.5 text-[11px] font-medium text-marrom">
              #{tag}
            </span>
          ))}
        </div>
      )}
      {footer && (
        <div className="mt-4 border-t border-border pt-3" onClick={(event) => event.stopPropagation()} onKeyDown={(event) => event.stopPropagation()}>
          {footer}
        </div>
      )}
    </motion.article>
  );
}
