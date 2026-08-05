import type { LucideIcon } from "lucide-react";
import { AlertCircle, Inbox, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";

export function LoadingState({ label }: { label: string }) {
  return (
    <div className="flex min-h-40 items-center justify-center gap-2 rounded-xl border border-dashed border-border p-8 text-sm text-muted-foreground" role="status">
      <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> {label}
    </div>
  );
}

export function EmptyState({ title, description, action, icon: Icon = Inbox }: {
  title: string;
  description?: string;
  action?: React.ReactNode;
  icon?: LucideIcon;
}) {
  return (
    <div className="rounded-2xl border border-dashed border-border bg-muted/20 px-6 py-12 text-center">
      <Icon className="mx-auto h-9 w-9 text-dourado" aria-hidden="true" />
      <h2 className="mt-4 font-serif text-2xl">{title}</h2>
      {description && <p className="mx-auto mt-2 max-w-lg text-sm text-muted-foreground">{description}</p>}
      {action && <div className="mt-6 flex justify-center">{action}</div>}
    </div>
  );
}

export function ErrorState({ title, description, onRetry, retryLabel }: {
  title: string;
  description?: string;
  onRetry?: () => void;
  retryLabel?: string;
}) {
  return (
    <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/5 p-5">
      <div className="flex items-start gap-3">
        <AlertCircle className="mt-0.5 h-5 w-5 shrink-0 text-destructive" aria-hidden="true" />
        <div className="min-w-0">
          <h2 className="font-medium text-destructive">{title}</h2>
          {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
          {onRetry && <Button type="button" variant="outline" size="sm" className="mt-3" onClick={onRetry}>{retryLabel}</Button>}
        </div>
      </div>
    </div>
  );
}
