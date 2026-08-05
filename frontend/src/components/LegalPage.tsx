import { Link } from "@tanstack/react-router";
import { ArrowLeft } from "lucide-react";
import { AppLogo } from "@/components/AppLogo";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { ThemeToggle } from "@/components/ThemeToggle";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import { useI18n } from "@/lib/i18n";

export type LegalSection = { title: string; paragraphs: string[] };

export function LegalPage({ title, introduction, sections }: { title: string; introduction: string; sections: LegalSection[] }) {
  const { t } = useI18n();
  return (
    <main id="main-content" className="min-h-screen bg-background">
      <header className="border-b border-border bg-background/95">
        <div className="mx-auto flex min-h-16 max-w-5xl items-center justify-between gap-3 px-4 sm:px-6">
          <Link to="/" aria-label="Scripto"><AppLogo /></Link>
          <div className="flex items-center"><LanguageSwitcher /><ThemeToggle /></div>
        </div>
      </header>
      <article className="mx-auto max-w-3xl px-4 py-10 sm:px-6 sm:py-14">
        <Link to="/" className="inline-flex items-center gap-2 rounded text-sm text-muted-foreground underline-offset-4 hover:text-foreground hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
          <ArrowLeft className="h-4 w-4" aria-hidden="true" /> {t("legal.back")}
        </Link>
        <p className="mt-8 text-xs font-semibold uppercase tracking-[0.18em] text-dourado">Scripto · Legal</p>
        <h1 className="mt-2 text-balance font-serif text-4xl sm:text-5xl">{title}</h1>
        <OrnamentDivider className="my-7" />
        <p className="text-base leading-8 text-muted-foreground">{introduction}</p>
        <div className="mt-10 space-y-9">
          {sections.map((section) => (
            <section key={section.title}>
              <h2 className="font-serif text-2xl text-foreground">{section.title}</h2>
              <div className="mt-3 space-y-3">
                {section.paragraphs.map((paragraph) => <p key={paragraph} className="text-sm leading-7 text-muted-foreground">{paragraph}</p>)}
              </div>
            </section>
          ))}
        </div>
        <p className="mt-12 border-t border-border pt-6 text-xs text-muted-foreground">{t("legal.updated")}</p>
      </article>
    </main>
  );
}
