import type { ReactNode } from "react";

export function DocumentDetails({
  title,
  metadata,
  tags = [],
  content,
  actions,
}: {
  title: string;
  metadata?: ReactNode;
  tags?: string[];
  content: string;
  actions?: ReactNode;
}) {
  return (
    <article className="space-y-5">
      <header>
        <h2 className="break-words font-serif text-3xl leading-tight">{title}</h2>
        {metadata && <div className="mt-2 text-sm text-muted-foreground">{metadata}</div>}
      </header>
      {tags.length > 0 && (
        <div className="flex flex-wrap gap-2" aria-label="Tags">
          {tags.map((tag) => <span key={tag} className="rounded-full bg-pessego/50 px-2.5 py-1 text-xs text-marrom">#{tag}</span>)}
        </div>
      )}
      <div className="whitespace-pre-wrap break-words rounded-xl border border-border bg-muted/20 p-4 text-sm leading-7">{content}</div>
      {actions && <footer className="flex flex-wrap justify-end gap-2">{actions}</footer>}
    </article>
  );
}
