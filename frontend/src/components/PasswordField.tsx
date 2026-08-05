import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useI18n } from "@/lib/i18n";
import { cn } from "@/lib/utils";

type Props = React.ComponentProps<typeof Input> & { error?: boolean };

export function PasswordField({ className, error, ...props }: Props) {
  const [visible, setVisible] = useState(false);
  const { t } = useI18n();
  const label = visible ? t("auth.hidePassword") : t("auth.showPassword");
  return (
    <div className="relative">
      <Input
        {...props}
        type={visible ? "text" : "password"}
        aria-invalid={error || undefined}
        className={cn("pr-11", className)}
      />
      <button
        type="button"
        onClick={() => setVisible((current) => !current)}
        aria-label={label}
        title={label}
        className="absolute right-2 top-1/2 inline-flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-md text-muted-foreground transition hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      >
        {visible ? <EyeOff className="h-4 w-4" aria-hidden="true" /> : <Eye className="h-4 w-4" aria-hidden="true" />}
      </button>
    </div>
  );
}
