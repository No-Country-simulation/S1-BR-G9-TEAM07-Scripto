import { useEffect, useState, type ReactNode } from "react";
import { useRouter } from "@tanstack/react-router";
import { currentSession } from "@/services/auth.service";
import { LoadingState } from "@/components/StatusState";
import { useI18n } from "@/lib/i18n";

export function AdminRoute({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { t } = useI18n();
  const [authorized, setAuthorized] = useState(false);

  useEffect(() => {
    const verify = () => {
      const session = currentSession();
      if (!session) {
        setAuthorized(false);
        void router.navigate({ to: "/admin/login" });
        return;
      }
      if (session.access !== "ADMIN") {
        setAuthorized(false);
        void router.navigate({ to: "/forbidden" });
        return;
      }
      setAuthorized(true);
    };
    verify();
    window.addEventListener("scripto:unauthorized", verify);
    return () => window.removeEventListener("scripto:unauthorized", verify);
  }, [router]);

  if (!authorized) return <div className="p-6"><LoadingState label={t("common.loading")} /></div>;
  return children;
}
