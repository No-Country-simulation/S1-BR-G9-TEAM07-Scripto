import { createFileRoute } from "@tanstack/react-router";
import { useCallback, useEffect, useState } from "react";
import { FileText, Flag, ShieldOff, Users } from "lucide-react";
import { PageHeader } from "@/components/PageHeader";
import { ErrorState, LoadingState } from "@/components/StatusState";
import { friendlyError } from "@/lib/errors";
import { useI18n } from "@/lib/i18n";
import { getAdminMetrics, type AdminMetrics } from "@/services/admin.service";

export const Route = createFileRoute("/admin/")({ component: AdminOverview });

function AdminOverview() {
  const { t } = useI18n();
  const [metrics, setMetrics] = useState<AdminMetrics | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setMetrics(await getAdminMetrics());
    } catch (currentError) {
      setError(friendlyError(currentError, t));
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => { void load(); }, [load]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10">
      <PageHeader eyebrow={t("admin.overview.eyebrow")} title={t("admin.overview.title")} description={t("admin.overview.subtitle")} />
      {loading ? <div className="mt-8"><LoadingState label={t("common.loading")} /></div> : error ? <div className="mt-8"><ErrorState title={t("common.error")} description={error} onRetry={() => void load()} retryLabel={t("common.retry")} /></div> : (
        <div className="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><Metric icon={Users} label={t("admin.metric.users")} value={metrics?.users ?? t("admin.metric.pending")} /><Metric icon={ShieldOff} label={t("admin.metric.suspended")} value={metrics?.suspendedUsers ?? t("admin.metric.pending")} /><Metric icon={FileText} label={t("admin.metric.documents")} value={metrics?.documents ?? t("admin.metric.pending")} /><Metric icon={Flag} label={t("admin.metric.reports")} value={metrics?.reportsOpen ?? 0} /></div>
      )}
    </div>
  );
}
function Metric({ icon: Icon, label, value }: { icon: typeof Users; label: string; value: string | number }) { return <article className="rounded-2xl border border-border bg-card p-5 shadow-sm"><div className="flex items-center justify-between gap-3"><p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">{label}</p><Icon className="h-5 w-5 text-dourado" /></div><p className={`mt-5 font-serif ${typeof value === "number" ? "text-4xl" : "text-xl"}`}>{value}</p></article>; }
