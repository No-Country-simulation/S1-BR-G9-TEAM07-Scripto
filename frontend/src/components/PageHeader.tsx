import type { ReactNode } from "react";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";

export function PageHeader({ eyebrow, title, description, actions }: {
  eyebrow: string;
  title: string;
  description?: string;
  actions?: ReactNode;
}) {
  return (
    <header>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="min-w-0">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-dourado">{eyebrow}</p>
          <h1 className="mt-1 text-balance font-serif text-3xl sm:text-4xl">{title}</h1>
          {description && <p className="mt-2 max-w-2xl text-sm leading-relaxed text-muted-foreground">{description}</p>}
        </div>
        {actions && <div className="flex shrink-0 flex-wrap gap-2">{actions}</div>}
      </div>
      <OrnamentDivider className="mt-6" />
    </header>
  );
}
