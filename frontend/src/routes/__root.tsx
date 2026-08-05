import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { HeadContent, Link, Outlet, Scripts, createRootRouteWithContext, useRouter } from "@tanstack/react-router";
import { useEffect, type ReactNode } from "react";
import { Toaster } from "@/components/ui/sonner";
import { AppLogo } from "@/components/AppLogo";
import { Button } from "@/components/ui/button";
import { I18nProvider, useI18n } from "@/lib/i18n";
import { ThemeProvider } from "@/lib/theme";
import { reportLovableError } from "@/lib/lovable-error-reporting";
import appCss from "../styles.css?url";

function NotFoundComponent() {
  const { t } = useI18n();
  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4 py-12">
      <div className="w-full max-w-lg text-center">
        <AppLogo className="justify-center" />
        <p className="mt-10 font-display text-8xl leading-none text-vinho" aria-hidden="true">404</p>
        <h1 className="mt-5 font-serif text-3xl">{t("notFound.title")}</h1>
        <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-muted-foreground">{t("notFound.body")}</p>
        <Link to="/" className="mt-7 inline-block"><Button className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("common.goHome")}</Button></Link>
      </div>
    </main>
  );
}

function ErrorComponent({ error, reset }: { error: Error; reset: () => void }) {
  const router = useRouter();
  const { t } = useI18n();
  useEffect(() => { reportLovableError(error, { boundary: "tanstack_root_error_component" }); }, [error]);

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4 py-12">
      <div className="w-full max-w-lg text-center">
        <AppLogo className="justify-center" />
        <h1 className="mt-10 font-serif text-3xl">{t("error.title")}</h1>
        <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-muted-foreground">{t("error.body")}</p>
        <div className="mt-7 flex flex-wrap justify-center gap-2">
          <Button onClick={() => { void router.invalidate(); reset(); }} className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("common.retry")}</Button>
          <Link to="/"><Button variant="outline">{t("common.goHome")}</Button></Link>
        </div>
      </div>
    </main>
  );
}

export const Route = createRootRouteWithContext<{ queryClient: QueryClient }>()({
  head: () => ({
    meta: [
      { charSet: "utf-8" },
      { name: "viewport", content: "width=device-width, initial-scale=1" },
      { title: "SCRIPTO — Onde suas leituras se organizam" },
      { name: "description", content: "SCRIPTO ajuda estudantes a organizar artigos, PDFs e anotações em uma biblioteca inteligente." },
      { property: "og:title", content: "SCRIPTO" },
      { property: "og:description", content: "Onde será que eu deixei? Está no Scripto." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
    links: [
      { rel: "stylesheet", href: appCss },
      { rel: "icon", href: "/logo-marrom-claro.svg", type: "image/svg+xml" },
      { rel: "preconnect", href: "https://fonts.googleapis.com" },
      { rel: "preconnect", href: "https://fonts.gstatic.com", crossOrigin: "anonymous" },
      { rel: "stylesheet", href: "https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Cormorant+Infant:ital,wght@0,400;0,500;0,600;0,700;1,400&family=Poiret+One&display=swap" },
    ],
  }),
  shellComponent: RootShell,
  component: RootComponent,
  notFoundComponent: NotFoundComponent,
  errorComponent: ErrorComponent,
});

function RootShell({ children }: { children: ReactNode }) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <head><HeadContent /></head>
      <body><a href="#main-content" className="skip-link">Pular para o conteúdo / Skip to content</a>{children}<Scripts /></body>
    </html>
  );
}

function RootComponent() {
  const { queryClient } = Route.useRouteContext();
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <I18nProvider>
          <Outlet />
          <Toaster richColors position="top-right" />
        </I18nProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
