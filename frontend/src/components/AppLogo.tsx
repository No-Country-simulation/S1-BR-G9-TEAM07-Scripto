import { useTheme } from "@/lib/theme";
import logoDark from "@/assets/logo-bege.svg";
import logoLight from "@/assets/logo-marrom.svg";
import { cn } from "@/lib/utils";

export function AppLogo({ compact = false, className }: { compact?: boolean; className?: string }) {
  const { theme } = useTheme();
  return (
    <span className={cn("inline-flex items-center gap-2.5", className)}>
      <img
        src={theme === "dark" ? logoDark : logoLight}
        alt=""
        aria-hidden="true"
        className={cn("shrink-0 rounded-full object-cover", compact ? "h-8 w-8" : "h-10 w-10")}
      />
      {!compact && <span className="font-serif text-xl font-semibold tracking-[0.08em]">SCRIPTO</span>}
      <span className="sr-only">Scripto</span>
    </span>
  );
}
