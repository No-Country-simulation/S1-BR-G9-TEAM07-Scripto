import { Link } from "@tanstack/react-router";
import { AppLogo } from "@/components/AppLogo";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { ThemeToggle } from "@/components/ThemeToggle";
import { AcanthusCorner, OrnamentDivider } from "@/components/ornaments/Acanthus";

export function AuthShell({ title, subtitle, children, footer }: {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
}) {
  return (
    <main className="relative min-h-screen overflow-hidden bg-background">
      <AcanthusCorner className="pointer-events-none absolute left-0 top-0 h-32 w-32 text-dourado/30 sm:h-44 sm:w-44" />
      <AcanthusCorner className="pointer-events-none absolute right-0 top-0 h-32 w-32 rotate-90 text-dourado/30 sm:h-44 sm:w-44" />
      <AcanthusCorner className="pointer-events-none absolute bottom-0 left-0 h-32 w-32 -rotate-90 text-dourado/30 sm:h-44 sm:w-44" />
      <AcanthusCorner className="pointer-events-none absolute bottom-0 right-0 h-32 w-32 rotate-180 text-dourado/30 sm:h-44 sm:w-44" />

      <div className="absolute right-3 top-3 z-10 flex items-center rounded-xl border border-border/70 bg-background/85 p-1 backdrop-blur sm:right-6 sm:top-6">
        <LanguageSwitcher />
        <ThemeToggle />
      </div>

      <div className="relative mx-auto flex min-h-screen w-full max-w-lg flex-col items-center justify-center px-4 py-20 sm:px-6">
        <Link to="/" className="mb-7 rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"><AppLogo /></Link>
        <section className="w-full rounded-2xl border border-border bg-card/95 p-5 shadow-[0_20px_70px_rgba(31,23,8,0.10)] backdrop-blur sm:p-8" aria-labelledby="auth-title">
          <h1 id="auth-title" className="font-serif text-3xl sm:text-4xl">{title}</h1>
          {subtitle && <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{subtitle}</p>}
          <OrnamentDivider className="my-6" />
          {children}
        </section>
        {footer && <div className="mt-6 text-center text-sm text-muted-foreground">{footer}</div>}
      </div>
    </main>
  );
}
