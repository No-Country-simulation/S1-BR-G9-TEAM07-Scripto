import { createFileRoute, Outlet, useRouter } from "@tanstack/react-router";
import { useEffect } from "react";
import { AppSidebar } from "@/components/layout/AppSidebar";
import { currentUser } from "@/services/auth.service";

export const Route = createFileRoute("/app")({ component: AppLayout });

function AppLayout() {
  const router = useRouter();
  useEffect(() => {
    const u = currentUser();
    if (!u) router.navigate({ to: "/login" });
    else if (u.deletionScheduledAt) router.navigate({ to: "/account-suspended" });
  }, [router]);

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <AppSidebar />
      <main className="h-screen min-w-0 flex-1 overflow-y-auto overflow-x-hidden">
        <Outlet />
      </main>
    </div>
  );
}
