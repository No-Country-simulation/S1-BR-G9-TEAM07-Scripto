import type { ReactNode } from "react";
import { Alert as BaseAlert, AlertDescription, AlertTitle } from "@/components/ui/alert";

export function Alert({ title, children, destructive = false }: { title?: string; children: ReactNode; destructive?: boolean }) {
  return (
    <BaseAlert variant={destructive ? "destructive" : "default"}>
      {title && <AlertTitle>{title}</AlertTitle>}
      <AlertDescription>{children}</AlertDescription>
    </BaseAlert>
  );
}
