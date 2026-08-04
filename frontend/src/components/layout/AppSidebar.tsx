import { Link, useRouter, useRouterState } from "@tanstack/react-router";
import { FilePlus2, Library, Compass, User, LogOut, Moon, Sun } from "lucide-react";
import { useI18n } from "@/lib/i18n";
import { useTheme } from "@/lib/theme";
import { logout } from "@/services/auth.service";
import { ScriptoWordmark, OrnamentDivider } from "@/components/ornaments/Acanthus";

type Item = { to: string; label: string; icon: typeof FilePlus2; exact?: boolean };
const items: Item[] = [
  { to: "/app", label: "app.nav.new", icon: FilePlus2, exact: true },
  { to: "/app/library", label: "app.nav.library", icon: Library },
  { to: "/app/explore", label: "app.nav.explore", icon: Compass },
  { to: "/app/profile", label: "app.nav.profile", icon: User },
];

export function AppSidebar() {
  const { t } = useI18n();
  const { theme, toggle } = useTheme();
  const router = useRouter();
  const pathname = useRouterState({ select: s => s.location.pathname });

  return (
    <aside className="hidden h-screen w-64 shrink-0 flex-col overflow-hidden border-r border-border bg-sidebar md:flex">
      <div className="px-5 py-5">
        <Link to="/app"><ScriptoWordmark /></Link>
      </div>
      <OrnamentDivider className="px-4" />
      <nav className="flex-1 space-y-1 px-3 py-4">
        {items.map(({ to, label, icon: Icon, exact }) => {
          const active = exact ? pathname === to : pathname.startsWith(to);
          return (
            <Link
              key={to} to={to}
              className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition ${
                active ? "bg-vinho text-vinho-foreground" : "text-sidebar-foreground hover:bg-sidebar-accent"
              }`}
            >
              <Icon className="h-4 w-4" aria-hidden="true" />
              <span>{t(label as never)}</span>
            </Link>
          );
        })}
      </nav>
      <div className="border-t border-sidebar-border p-3">
        <button
          onClick={toggle}
          className="mb-2 flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-sidebar-foreground hover:bg-sidebar-accent"
        >
          {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          {theme === "dark" ? "Tema claro" : "Tema escuro"}
        </button>
        <button
          onClick={() => { logout(); router.navigate({ to: "/" }); }}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-sidebar-foreground hover:bg-sidebar-accent"
        >
          <LogOut className="h-4 w-4" /> {t("app.nav.logout")}
        </button>
      </div>
    </aside>
  );
}
