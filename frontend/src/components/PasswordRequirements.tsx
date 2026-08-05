import { Check, Circle } from "lucide-react";
import { useI18n } from "@/lib/i18n";
import { passwordRules } from "@/lib/validation";

export function PasswordRequirements({ password }: { password: string }) {
  const { t } = useI18n();
  const items = [
    ["length", t("auth.passwordRequirements.length")],
    ["upper", t("auth.passwordRequirements.upper")],
    ["lower", t("auth.passwordRequirements.lower")],
    ["number", t("auth.passwordRequirements.number")],
    ["symbol", t("auth.passwordRequirements.symbol")],
  ] as const;

  return (
    <div className="rounded-lg border border-border bg-muted/35 p-3" aria-live="polite">
      <p className="text-xs font-medium text-foreground">{t("auth.passwordRequirements.title")}</p>
      <ul className="mt-2 grid gap-1 sm:grid-cols-2">
        {items.map(([key, label]) => {
          const valid = passwordRules[key](password);
          return (
            <li key={key} className={`flex items-center gap-1.5 text-xs ${valid ? "text-verde" : "text-muted-foreground"}`}>
              {valid ? <Check className="h-3.5 w-3.5" aria-hidden="true" /> : <Circle className="h-3 w-3" aria-hidden="true" />}
              <span>{label}</span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
