import { createFileRoute, Outlet, Link, useRouter, useRouterState } from "@tanstack/react-router";
import { useEffect } from "react";
import { LayoutDashboard, Users, FileText, Flag, LogOut } from "lucide-react";
import { currentUser, logout } from "@/services/auth.service";
import { ScriptoWordmark, OrnamentDivider } from "@/components/ornaments/Acanthus";

export const Route = createFileRoute("/admin")({ component: AdminLayout });

const items = [
  { to: "/admin", label: "Visão geral", icon: LayoutDashboard, exact: true },
  { to: "/admin/users", label: "Usuários", icon: Users },
  { to: "/admin/documents", label: "Documentos", icon: FileText },
  { to: "/admin/reports", label: "Denúncias", icon: Flag },
];

function AdminLayout() {
  const router = useRouter();
  const pathname = useRouterState({ select: s => s.location.pathname });
  useEffect(() => {
    const u = currentUser();
    if (!u || u.role !== "admin") router.navigate({ to: "/admin/login" });
  }, [router]);

  return (
    <div className="flex min-h-screen bg-background">
      <aside className="hidden w-64 shrink-0 flex-col border-r border-border bg-marrom text-pessego md:flex">
        <div className="px-5 py-5"><ScriptoWordmark className="text-pessego" /></div>
        <p className="px-5 pb-2 text-[10px] uppercase tracking-widest text-dourado">Admin</p>
        <OrnamentDivider className="px-4" />
        <nav className="flex-1 space-y-1 px-3 py-4">
          {items.map(({ to, label, icon: Icon, exact }) => {
            const active = exact ? pathname === to : pathname.startsWith(to);
            return (
              <Link key={to} to={to}
                className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm ${active ? "bg-dourado text-marrom" : "hover:bg-white/5"}`}>
                <Icon className="h-4 w-4" /> {label}
              </Link>
            );
          })}
        </nav>
        <button onClick={() => { logout(); router.navigate({ to: "/" }); }}
          className="m-3 flex items-center gap-3 rounded-lg px-3 py-2 text-sm hover:bg-white/5">
          <LogOut className="h-4 w-4" /> Sair
        </button>
      </aside>
      <main className="flex-1"><Outlet /></main>
    </div>
  );
}
