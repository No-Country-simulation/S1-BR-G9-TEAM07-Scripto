import { createFileRoute, Outlet } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { UserSidebar } from "@/components/UserSidebar";

export const Route = createFileRoute("/app")({ component: AppLayout });

function AppLayout() {
  return (
    <ProtectedRoute>
      <div className="flex min-h-screen bg-background md:h-screen md:overflow-hidden">
        <UserSidebar />
        <main id="main-content" className="min-w-0 flex-1 md:h-screen md:overflow-y-auto md:overflow-x-hidden">
          <Outlet />
        </main>
      </div>
    </ProtectedRoute>
  );
}
