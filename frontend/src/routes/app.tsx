import { createFileRoute, Outlet } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { UserSidebar } from "@/components/UserSidebar";

export const Route = createFileRoute("/app")({ component: AppLayout });

function AppLayout() {
  return (
    <ProtectedRoute>
      <div className="flex min-h-screen flex-col bg-background lg:flex-row">
        <UserSidebar />
        <main id="main-content" className="min-w-0 flex-1 overflow-x-hidden">
          <Outlet />
        </main>
      </div>
    </ProtectedRoute>
  );
}
