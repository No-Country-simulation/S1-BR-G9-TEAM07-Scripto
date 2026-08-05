import { Link } from "@tanstack/react-router";
import { Menu } from "lucide-react";
import { AppLogo } from "@/components/AppLogo";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { ThemeToggle } from "@/components/ThemeToggle";
import { Button } from "@/components/ui/button";
import { Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { useI18n } from "@/lib/i18n";

export function TopBar() {
  const { t } = useI18n();
  const links = [
    ["#features", t("nav.features")],
    ["#how", t("nav.how")],
    ["#team", t("nav.team")],
  ] as const;

  return (
    <header className="sticky top-0 z-50 border-b border-border/70 bg-background/90 backdrop-blur-xl">
      <div className="mx-auto flex min-h-16 max-w-7xl items-center justify-between gap-3 px-4 md:px-8">
        <Link to="/" className="rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" aria-label="Scripto">
          <AppLogo />
        </Link>

        <nav className="hidden items-center gap-7 text-sm text-muted-foreground md:flex" aria-label="Principal">
          {links.map(([href, label]) => <a key={href} href={href} className="rounded px-1 py-2 transition hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">{label}</a>)}
        </nav>

        <div className="hidden items-center gap-1 sm:flex">
          <LanguageSwitcher />
          <ThemeToggle />
          <Link to="/login"><Button variant="ghost" size="sm">{t("nav.login")}</Button></Link>
          <Link to="/register"><Button size="sm" className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("nav.register")}</Button></Link>
        </div>

        <div className="flex items-center gap-1 sm:hidden">
          <ThemeToggle />
          <Sheet>
            <SheetTrigger asChild><Button type="button" variant="outline" size="icon" aria-label={t("common.openMenu")}><Menu className="h-5 w-5" aria-hidden="true" /></Button></SheetTrigger>
            <SheetContent side="right" className="flex w-[min(88vw,22rem)] flex-col">
              <SheetHeader className="text-left"><SheetTitle><AppLogo /></SheetTitle></SheetHeader>
              <nav className="mt-6 flex flex-col gap-1" aria-label="Principal">
                {links.map(([href, label]) => <SheetClose asChild key={href}><a href={href} className="rounded-lg px-3 py-3 text-sm font-medium hover:bg-muted">{label}</a></SheetClose>)}
              </nav>
              <div className="mt-auto space-y-2 border-t border-border pt-4">
                <LanguageSwitcher showLabel className="w-full justify-start" />
                <SheetClose asChild><Link to="/login"><Button variant="outline" className="w-full">{t("nav.login")}</Button></Link></SheetClose>
                <SheetClose asChild><Link to="/register"><Button className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("nav.register")}</Button></Link></SheetClose>
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>
    </header>
  );
}
