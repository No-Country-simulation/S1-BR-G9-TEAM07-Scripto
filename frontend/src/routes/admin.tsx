import { createFileRoute, Outlet, useRouterState } from "@tanstack/react-router";
import { AdminRoute } from "@/components/AdminRoute";
import { AdminSidebar } from "@/components/AdminSidebar";

export const Route = createFileRoute("/admin")({ component: AdminLayout });

function AdminLayout() {
  const pathname = useRouterState({ select: (state) => state.location.pathname });

  // The file-route hierarchy keeps /admin/login under /admin. It must remain
  // public, so the protected shell is deliberately bypassed for this path.
  if (pathname === "/admin/login") return <Outlet />;

  return (
    <AdminRoute>
      <div className="flex min-h-screen bg-background md:h-screen md:overflow-hidden">
        <AdminSidebar />
        <main id="main-content" className="min-w-0 flex-1 md:h-screen md:overflow-x-hidden md:overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </AdminRoute>
  );
}
