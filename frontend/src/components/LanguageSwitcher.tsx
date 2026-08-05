import { Languages } from "lucide-react";
import { useI18n } from "@/lib/i18n";
import { cn } from "@/lib/utils";

export function LanguageSwitcher({
  showLabel = false,
  className,
}: {
  showLabel?: boolean;
  className?: string;
}) {
  const { lang, setLang, t } = useI18n();
  const next = lang === "pt-BR" ? "en" : "pt-BR";
  const currentLanguage = lang === "pt-BR" ? "Português" : "English";

  return (
    <button
      type="button"
      onClick={() => setLang(next)}
      aria-label={t("common.changeLanguage")}
      title={t("common.changeLanguage")}
      className={cn(
        "inline-flex min-h-10 items-center justify-center gap-2 rounded-lg px-2.5 text-sm text-muted-foreground transition hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
        className,
      )}
    >
      <Languages className="h-4 w-4" aria-hidden="true" />
      <span>{showLabel ? currentLanguage : lang === "pt-BR" ? "PT" : "EN"}</span>
    </button>
  );
}
