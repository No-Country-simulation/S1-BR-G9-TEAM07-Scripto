import { Link, useRouter, useRouterState } from "@tanstack/react-router";
import { FileText, Flag, LayoutDashboard, LogOut, Menu, Users } from "lucide-react";
import { AppLogo } from "@/components/AppLogo";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { ThemeToggle } from "@/components/ThemeToggle";
import { Button } from "@/components/ui/button";
import { Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { useI18n } from "@/lib/i18n";
import { cn } from "@/lib/utils";
import { logout } from "@/services/auth.service";

const navItems = [
  { to: "/admin", label: "admin.nav.overview", icon: LayoutDashboard, exact: true },
  { to: "/admin/users", label: "admin.nav.users", icon: Users, exact: false },
  { to: "/admin/documents", label: "admin.nav.documents", icon: FileText, exact: false },
  { to: "/admin/reports", label: "admin.nav.reports", icon: Flag, exact: false },
] as const;

export function AdminSidebar() {
  const { t } = useI18n();
  const router = useRouter();
  const pathname = useRouterState({ select: (state) => state.location.pathname });

  function signOut() {
    logout();
    void router.navigate({ to: "/" });
  }

  const navigation = (mobile = false) => (
    <nav className="space-y-1" aria-label={t("admin.sidebar.area")}>
      {navItems.map(({ to, label, icon: Icon, exact }) => {
        const active = exact ? pathname === to || pathname === `${to}/` : pathname.startsWith(to);
        const link = (
          <Link
            key={to}
            to={to}
            aria-current={active ? "page" : undefined}
            className={cn(
              "flex min-h-11 items-center gap-3 rounded-xl px-3 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-dourado",
              active ? "bg-dourado text-marrom shadow-sm" : "text-pessego hover:bg-white/10",
            )}
          >
            <Icon className="h-4 w-4" aria-hidden="true" /> {t(label)}
          </Link>
        );
        return mobile ? <SheetClose asChild key={to}>{link}</SheetClose> : link;
      })}
    </nav>
  );

  return (
    <>
      <aside className="hidden h-screen w-64 shrink-0 flex-col border-r border-white/10 bg-marrom text-pessego md:flex">
        <div className="border-b border-white/10 px-5 py-5"><Link to="/admin" className="text-pessego"><AppLogo /></Link></div>
        <p className="px-5 pt-4 text-[10px] font-semibold uppercase tracking-[0.2em] text-dourado">{t("admin.sidebar.area")}</p>
        <div className="flex-1 overflow-y-auto px-3 py-4">{navigation()}</div>
        <div className="space-y-1 border-t border-white/10 p-3">
          <LanguageSwitcher showLabel className="w-full justify-start text-pessego hover:bg-white/10 hover:text-white" />
          <ThemeToggle showLabel className="w-full justify-start text-pessego hover:bg-white/10 hover:text-white" />
          <button type="button" onClick={signOut} className="flex min-h-10 w-full items-center gap-3 rounded-lg px-2.5 text-sm text-pessego hover:bg-white/10 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-dourado">
            <LogOut className="h-4 w-4" aria-hidden="true" /> {t("app.nav.logout")}
          </button>
        </div>
      </aside>

      <div className="sticky top-0 z-40 flex min-h-16 items-center justify-between border-b border-border bg-marrom px-4 text-pessego md:hidden">
        <Link to="/admin" className="text-pessego" aria-label="Scripto Admin"><AppLogo compact /></Link>
        <Sheet>
          <SheetTrigger asChild><Button size="icon" variant="outline" className="border-white/20 bg-transparent text-pessego hover:bg-white/10" aria-label={t("common.openMenu")}><Menu className="h-5 w-5" /></Button></SheetTrigger>
          <SheetContent side="left" className="flex w-[min(88vw,22rem)] flex-col bg-marrom p-0 text-pessego">
            <SheetHeader className="border-b border-white/10 px-5 py-5 text-left">
              <SheetTitle className="text-pessego"><AppLogo /></SheetTitle>
              <p className="text-xs uppercase tracking-widest text-dourado">{t("admin.sidebar.area")}</p>
            </SheetHeader>
            <div className="flex-1 overflow-y-auto p-4">{navigation(true)}</div>
            <div className="space-y-1 border-t border-white/10 p-4">
              <LanguageSwitcher showLabel className="w-full justify-start text-pessego hover:bg-white/10" />
              <ThemeToggle showLabel className="w-full justify-start text-pessego hover:bg-white/10" />
              <button type="button" onClick={signOut} className="flex min-h-10 w-full items-center gap-3 rounded-lg px-2.5 text-sm hover:bg-white/10"><LogOut className="h-4 w-4" /> {t("app.nav.logout")}</button>
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </>
  );
}
