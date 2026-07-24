import { createFileRoute, useRouter } from "@tanstack/react-router";
import { AlertTriangle } from "lucide-react";
import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/button";
import { currentUser, reactivate, logout } from "@/services/auth.service";

export const Route = createFileRoute("/account-suspended")({ component: Page });

function Page() {
  const router = useRouter();
  const user = currentUser();
  const scheduled = user?.deletionScheduledAt ? new Date(user.deletionScheduledAt) : null;
  const daysLeft = scheduled ? Math.max(0, 30 - Math.floor((Date.now() - scheduled.getTime()) / 86400000)) : 30;

  return (
    <AuthShell title="Conta em processo de exclusão">
      <div className="flex items-start gap-3 rounded-lg border border-vinho/30 bg-vinho/5 p-4 text-sm">
        <AlertTriangle className="mt-0.5 h-5 w-5 text-vinho" aria-hidden="true" />
        <div>
          <p className="font-medium text-foreground">Sua conta está agendada para exclusão.</p>
          <p className="mt-1 text-taupe">
            Restam <strong className="text-vinho">{daysLeft} dias</strong> antes da remoção definitiva.
            Você pode reativar a conta a qualquer momento neste período.
          </p>
        </div>
      </div>
      <div className="mt-6 flex gap-2">
        <Button
          className="flex-1 bg-vinho text-vinho-foreground hover:bg-vinho/90"
          onClick={async () => { await reactivate(); router.navigate({ to: "/app" }); }}
        >
          Reativar minha conta
        </Button>
        <Button variant="outline" onClick={() => { logout(); router.navigate({ to: "/" }); }}>Sair</Button>
      </div>
    </AuthShell>
  );
}
