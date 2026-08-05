import { createFileRoute, Link } from "@tanstack/react-router";
import { ShieldX } from "lucide-react";
import { AppLogo } from "@/components/AppLogo";
import { Button } from "@/components/ui/button";
import { useI18n } from "@/lib/i18n";

export const Route = createFileRoute("/forbidden")({ component: ForbiddenPage });

function ForbiddenPage() {
  const { lang } = useI18n();
  const isPt = lang === "pt-BR";

  return (
    <main id="main-content" className="flex min-h-screen items-center justify-center bg-background px-4 py-12">
      <section className="w-full max-w-lg text-center" aria-labelledby="forbidden-title">
        <AppLogo className="justify-center" />
        <ShieldX className="mx-auto mt-10 h-16 w-16 text-vinho" aria-hidden="true" />
        <p className="mt-4 font-display text-5xl leading-none text-vinho" aria-hidden="true">403</p>
        <h1 id="forbidden-title" className="mt-5 font-serif text-3xl">
          {isPt ? "Acesso não autorizado" : "Access not authorized"}
        </h1>
        <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-muted-foreground">
          {isPt
            ? "Sua conta está autenticada, mas não possui permissão para acessar esta área."
            : "Your account is authenticated but does not have permission to access this area."}
        </p>
        <div className="mt-7 flex flex-col justify-center gap-2 sm:flex-row">
          <Link to="/app">
            <Button className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90 sm:w-auto">
              {isPt ? "Voltar à área do usuário" : "Return to user area"}
            </Button>
          </Link>
          <Link to="/">
            <Button variant="outline" className="w-full sm:w-auto">
              {isPt ? "Ir ao início" : "Go home"}
            </Button>
          </Link>
        </div>
      </section>
    </main>
  );
}
