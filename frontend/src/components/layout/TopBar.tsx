import { Link } from "@tanstack/react-router";
import { Moon, Sun, Languages } from "lucide-react";
import { useTheme } from "@/lib/theme";
import { useI18n } from "@/lib/i18n";
import { ScriptoWordmark } from "@/components/ornaments/Acanthus";
import { Button } from "@/components/ui/button";

export function TopBar() {
  const { theme, toggle } = useTheme();
  const { lang, setLang, t } = useI18n();

  return (
    <header className="sticky top-0 z-50 border-b border-border/60 bg-background/70 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 md:px-8">
        <Link to="/" className="flex items-center gap-2 text-foreground">
          <ScriptoWordmark />
        </Link>

        <nav className="hidden items-center gap-8 text-sm text-taupe md:flex">
          <a href="#features" className="hover:text-foreground">{t("nav.features")}</a>
          <a href="#how" className="hover:text-foreground">{t("nav.how")}</a>
          <a href="#team" className="hover:text-foreground">{t("nav.team")}</a>
        </nav>

        <div className="flex items-center gap-1.5">
          <button
            onClick={() => setLang(lang === "pt-BR" ? "en" : "pt-BR")}
            className="inline-flex items-center gap-1 rounded-md px-2 py-1.5 text-xs text-muted-foreground hover:bg-muted"
            aria-label="Trocar idioma"
          >
            <Languages className="h-4 w-4" aria-hidden="true" />
            {lang === "pt-BR" ? "PT" : "EN"}
          </button>
          <button
            onClick={toggle}
            className="rounded-md p-1.5 text-muted-foreground hover:bg-muted"
            aria-label="Alternar tema"
          >
            {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          </button>
          <Link to="/login">
            <Button variant="ghost" size="sm">{t("nav.login")}</Button>
          </Link>
          <Link to="/register">
            <Button size="sm" className="bg-vinho text-vinho-foreground hover:bg-vinho/90">
              {t("nav.register")}
            </Button>
          </Link>
        </div>
      </div>
    </header>
  );
}
