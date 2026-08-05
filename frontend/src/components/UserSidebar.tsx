import { useEffect, useState } from "react";
import { Link, useRouter, useRouterState } from "@tanstack/react-router";
import { ChevronLeft, ChevronRight, Compass, FilePlus2, Library, LogOut, Menu, User } from "lucide-react";
import { AppLogo } from "@/components/AppLogo";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { ThemeToggle } from "@/components/ThemeToggle";
import { Button } from "@/components/ui/button";
import { Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { logout } from "@/services/auth.service";
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

  useEffect(() => {
    setCollapsed(localStorage.getItem(STORAGE_KEY) === "true");
  }, []);

  function toggleCollapsed() {
    setCollapsed((current) => {
      const next = !current;
      localStorage.setItem(STORAGE_KEY, String(next));
      return next;
    });
  }

  function signOut() {
    logout();
    void router.navigate({ to: "/" });
  }

  const navigation = (mobile = false) => (
    <nav className="space-y-1" aria-label={t("app.mobile.navigation")}>
      {navItems.map(({ to, label, icon: Icon, exact }) => {
        const active = exact ? pathname === to || pathname === `${to}/` : pathname.startsWith(to);
        const link = (
          <Link
            key={to}
            to={to}
            aria-current={active ? "page" : undefined}
            title={!mobile && collapsed ? t(label) : undefined}
            className={cn(
              "flex min-h-11 items-center gap-3 rounded-xl px-3 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring",
              active ? "bg-sidebar-primary text-sidebar-primary-foreground shadow-sm" : "text-sidebar-foreground hover:bg-sidebar-accent",
              !mobile && collapsed && "justify-center px-2",
            )}
          >
            <Icon className="h-4 w-4 shrink-0" aria-hidden="true" />
            {(mobile || !collapsed) && <span>{t(label)}</span>}
          </Link>
        );
        return mobile ? <SheetClose asChild key={to}>{link}</SheetClose> : link;
      })}
    </nav>
  );

  return (
    <>
      <aside className={cn("hidden h-screen shrink-0 flex-col border-r border-sidebar-border bg-sidebar transition-[width] duration-200 md:flex", collapsed ? "w-20" : "w-64")}>
        <div className={cn("flex min-h-20 items-center border-b border-sidebar-border px-4", collapsed ? "justify-center" : "justify-between")}>
          <Link to="/app" aria-label="Scripto"><AppLogo compact={collapsed} /></Link>
          {!collapsed && (
            <button type="button" onClick={toggleCollapsed} aria-label={t("common.collapseMenu")} className="rounded-lg p-2 text-muted-foreground hover:bg-sidebar-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring">
              <ChevronLeft className="h-4 w-4" aria-hidden="true" />
            </button>
          )}
        </div>
        {collapsed && (
          <button type="button" onClick={toggleCollapsed} aria-label={t("common.expandMenu")} className="mx-auto mt-3 rounded-lg p-2 text-muted-foreground hover:bg-sidebar-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring">
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
          </button>
        )}
        <div className="flex-1 overflow-y-auto px-3 py-4">{navigation()}</div>
        <div className="space-y-1 border-t border-sidebar-border p-3">
          <LanguageSwitcher showLabel={!collapsed} className={cn("w-full", collapsed ? "px-0" : "justify-start")} />
          <ThemeToggle showLabel={!collapsed} className={cn("w-full", collapsed ? "px-0" : "justify-start")} />
          <button type="button" onClick={signOut} title={collapsed ? t("app.nav.logout") : undefined} className={cn("flex min-h-10 w-full items-center gap-3 rounded-lg px-2.5 text-sm text-sidebar-foreground transition hover:bg-sidebar-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring", collapsed && "justify-center px-0")}>
            <LogOut className="h-4 w-4" aria-hidden="true" />
            {!collapsed && <span>{t("app.nav.logout")}</span>}
          </button>
        </div>
      </aside>

      <div className="sticky top-0 z-40 flex min-h-16 items-center justify-between border-b border-border bg-background/95 px-4 backdrop-blur md:hidden">
        <Link to="/app" aria-label="Scripto"><AppLogo compact /></Link>
        <Sheet>
          <SheetTrigger asChild>
            <Button type="button" size="icon" variant="outline" aria-label={t("common.openMenu")}><Menu className="h-5 w-5" aria-hidden="true" /></Button>
          </SheetTrigger>
          <SheetContent side="left" className="flex w-[min(88vw,22rem)] flex-col p-0">
            <SheetHeader className="border-b border-border px-5 py-5 text-left">
              <SheetTitle><AppLogo /></SheetTitle>
              <p className="text-xs uppercase tracking-widest text-dourado">{t("app.sidebar.userArea")}</p>
            </SheetHeader>
            <div className="flex-1 overflow-y-auto p-4">{navigation(true)}</div>
            <div className="space-y-1 border-t border-border p-4">
              <LanguageSwitcher showLabel className="w-full justify-start" />
              <ThemeToggle showLabel className="w-full justify-start" />
              <button type="button" onClick={signOut} className="flex min-h-10 w-full items-center gap-3 rounded-lg px-2.5 text-sm hover:bg-muted">
                <LogOut className="h-4 w-4" aria-hidden="true" /> {t("app.nav.logout")}
              </button>
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </>
  );
}
