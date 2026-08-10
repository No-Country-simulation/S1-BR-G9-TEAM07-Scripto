import { useEffect, useMemo, useState } from "react";
import { Link, useRouter, useRouterState } from "@tanstack/react-router";
import {
  ChevronRight,
  Compass,
  FilePlus2,
  Library,
  LogOut,
  Menu,
  User,
  X,
} from "lucide-react";
import { AppLogo } from "@/components/AppLogo";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { ThemeToggle } from "@/components/ThemeToggle";
import { logout } from "@/services/auth.service";
import { currentSession } from "@/services/session";
import { useI18n } from "@/lib/i18n";
import { cn } from "@/lib/utils";

const STORAGE_KEY = "scripto-sidebar-collapsed";

const navItems = [
  { to: "/app", label: "app.nav.new", icon: FilePlus2, exact: true },
  { to: "/app/library", label: "app.nav.library", icon: Library, exact: false },
  { to: "/app/explore", label: "app.nav.explore", icon: Compass, exact: false },
  { to: "/app/profile", label: "app.nav.profile", icon: User, exact: false },
] as const;

export function UserSidebar() {
  const { t } = useI18n();
  const router = useRouter();
  const pathname = useRouterState({ select: (state) => state.location.pathname });
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [session, setSession] = useState(() => currentSession());

  useEffect(() => {
    setCollapsed(localStorage.getItem(STORAGE_KEY) === "true");
    setSession(currentSession());
  }, []);

  useEffect(() => {
    if (!mobileOpen) return;
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setMobileOpen(false);
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [mobileOpen]);

  const initials = useMemo(() => {
    const name = session?.user.fullName?.trim() || "Scripto";
    return name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join("");
  }, [session]);

  function toggleCollapsed() {
    setCollapsed((current) => {
      const next = !current;
      localStorage.setItem(STORAGE_KEY, String(next));
      return next;
    });
  }

  function signOut() {
    logout();
    setMobileOpen(false);
    void router.navigate({ to: "/" });
  }

  const SidebarContent = ({ mobile = false }: { mobile?: boolean }) => (
    <div className="flex h-full flex-col">
      <div className="flex min-h-20 items-center justify-center border-b border-sidebar-border px-3 py-3">
        <Link
          to="/app"
          onClick={() => mobile && setMobileOpen(false)}
          className="flex w-full items-center justify-center rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
          aria-label="Scripto"
        >
          <AppLogo compact={!mobile && collapsed} />
        </Link>
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4" aria-label={t("app.mobile.navigation")}>
        {navItems.map(({ to, label, icon: Icon, exact }) => {
          const active = exact ? pathname === to || pathname === `${to}/` : pathname.startsWith(to);
          return (
            <Link
              key={to}
              to={to}
              onClick={() => mobile && setMobileOpen(false)}
              aria-current={active ? "page" : undefined}
              title={!mobile && collapsed ? t(label) : undefined}
              className={cn(
                "flex min-h-11 items-center gap-3 rounded-lg px-3 text-sm font-medium transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring",
                active
                  ? "bg-vinho text-vinho-foreground shadow-sm"
                  : "text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-foreground",
                !mobile && collapsed && "justify-center px-2",
              )}
            >
              <Icon className="h-[18px] w-[18px] shrink-0" aria-hidden="true" />
              {(mobile || !collapsed) && <span>{t(label)}</span>}
            </Link>
          );
        })}
      </nav>

      <div className="space-y-2 border-t border-sidebar-border p-3">
        <LanguageSwitcher
          showLabel={mobile || !collapsed}
          className={cn(
            "w-full text-sm font-medium",
            mobile || !collapsed ? "justify-start px-3" : "justify-center px-0",
          )}
        />
        <ThemeToggle
          showLabel={mobile || !collapsed}
          className={cn(
            "w-full text-sm font-medium",
            mobile || !collapsed ? "justify-start px-3" : "justify-center px-0",
          )}
        />

        {(mobile || !collapsed) ? (
          <div className="flex items-center gap-3 rounded-lg bg-sidebar-accent px-3 py-2.5">
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-vinho text-vinho-foreground">
              <span className="text-xs font-bold">{initials}</span>
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-sidebar-foreground">
                {session?.user.fullName || t("app.sidebar.userArea")}
              </p>
              <p className="truncate text-xs text-muted-foreground">{session?.user.email || "Scripto"}</p>
            </div>
            <button
              type="button"
              onClick={signOut}
              className="rounded-md p-1 text-muted-foreground transition-colors hover:bg-background/60 hover:text-destructive focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
              title={t("app.nav.logout")}
              aria-label={t("app.nav.logout")}
            >
              <LogOut className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={signOut}
            title={t("app.nav.logout")}
            aria-label={t("app.nav.logout")}
            className="mx-auto flex h-8 w-8 items-center justify-center rounded-full bg-vinho text-vinho-foreground transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
          >
            <span className="text-xs font-bold">{initials}</span>
          </button>
        )}
      </div>
    </div>
  );

  return (
    <>
      <aside
        className={cn(
          "relative z-40 hidden h-dvh shrink-0 flex-col border-r border-sidebar-border bg-sidebar transition-[width] duration-300 ease-in-out lg:sticky lg:top-0 lg:self-start lg:flex",
          collapsed ? "w-16" : "w-60",
        )}
        aria-label={t("app.mobile.navigation")}
      >
        <SidebarContent />
        <button
          type="button"
          onClick={toggleCollapsed}
          className="absolute -right-3 top-20 flex h-6 w-6 items-center justify-center rounded-full border border-sidebar-border bg-sidebar text-muted-foreground shadow-sm transition hover:text-sidebar-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
          aria-label={collapsed ? t("common.expandMenu") : t("common.collapseMenu")}
        >
          <ChevronRight
            className={cn("h-3 w-3 transition-transform duration-300", !collapsed && "rotate-180")}
            aria-hidden="true"
          />
        </button>
      </aside>

      <div className="sticky top-0 z-40 flex h-14 shrink-0 items-center justify-between border-b border-border bg-[#E8DED2]/95 px-4 backdrop-blur dark:bg-background/95 lg:hidden">
        <Link to="/app" className="flex items-center justify-center rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" aria-label="Scripto">
          <AppLogo compact />
        </Link>
        <div className="flex items-center gap-1">
          <ThemeToggle className="h-9 min-h-9 w-9 px-0" />
          <button
            type="button"
            onClick={() => setMobileOpen(true)}
            className="flex h-9 w-9 items-center justify-center rounded-lg text-foreground transition hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            aria-label={t("common.openMenu")}
          >
            <Menu className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>
      </div>

      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden" role="dialog" aria-modal="true" aria-label={t("app.mobile.navigation")}>
          <button
            type="button"
            className="absolute inset-0 h-full w-full animate-in fade-in bg-foreground/40 backdrop-blur-sm duration-200"
            onClick={() => setMobileOpen(false)}
            aria-label={t("common.close")}
          />
          <div className="absolute left-0 top-0 h-full w-[min(18rem,88vw)] animate-in slide-in-from-left border-r border-sidebar-border bg-sidebar shadow-2xl duration-200">
            <button
              type="button"
              onClick={() => setMobileOpen(false)}
              className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground transition hover:bg-sidebar-accent hover:text-sidebar-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
              aria-label={t("common.close")}
            >
              <X className="h-[18px] w-[18px]" aria-hidden="true" />
            </button>
            <SidebarContent mobile />
          </div>
        </div>
      )}
    </>
  );
}
