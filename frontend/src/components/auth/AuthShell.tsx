import { Link } from "@tanstack/react-router";
import { AcanthusCorner, ScriptoWordmark, OrnamentDivider } from "@/components/ornaments/Acanthus";

export function AuthShell({ title, subtitle, children, footer }: {
  title: string; subtitle?: string; children: React.ReactNode; footer?: React.ReactNode;
}) {
  return (
    <div className="relative min-h-screen bg-background">
      <AcanthusCorner className="pointer-events-none absolute left-0 top-0 h-40 w-40 text-dourado/40" />
      <AcanthusCorner className="pointer-events-none absolute right-0 top-0 h-40 w-40 rotate-90 text-dourado/40" />
      <AcanthusCorner className="pointer-events-none absolute bottom-0 left-0 h-40 w-40 -rotate-90 text-dourado/40" />
      <AcanthusCorner className="pointer-events-none absolute bottom-0 right-0 h-40 w-40 rotate-180 text-dourado/40" />

      <div className="mx-auto flex min-h-screen max-w-md flex-col items-center justify-center px-4 py-12">
        <Link to="/" className="mb-8"><ScriptoWordmark className="text-2xl" /></Link>
        <div className="w-full rounded-2xl border border-bege bg-card p-8 shadow-sm">
          <h1 className="font-serif text-3xl">{title}</h1>
          {subtitle && <p className="mt-1 text-sm text-taupe">{subtitle}</p>}
          <OrnamentDivider className="my-6" />
          {children}
        </div>
        {footer && <div className="mt-6 text-sm text-taupe">{footer}</div>}
      </div>
    </div>
  );
}
