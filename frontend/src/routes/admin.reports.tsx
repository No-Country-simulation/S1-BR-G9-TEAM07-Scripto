import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import { listReports, resolveReport, type Report } from "@/services/admin.service";
import { remove, setPublic } from "@/services/documents.service";

export const Route = createFileRoute("/admin/reports")({ component: AdminReports });

function AdminReports() {
  const [reports, setReports] = useState<Report[]>([]);
  const load = () => listReports().then(setReports);
  useEffect(() => { load(); }, []);

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <p className="text-xs uppercase tracking-widest text-dourado">Moderação</p>
      <h1 className="font-serif text-4xl">Denúncias</h1>
      <OrnamentDivider className="mt-6" />

      <div className="mt-6 space-y-3">
        {reports.length === 0 && (
          <p className="rounded-xl border border-dashed border-bege p-10 text-center text-taupe">
            Nenhuma denúncia por enquanto.
          </p>
        )}
        {reports.map(r => (
          <div key={r.id} className="rounded-xl border border-bege bg-card p-5">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="font-serif text-xl">{r.docTitle}</p>
                <p className="mt-1 text-sm text-taupe">Motivo: <strong>{r.reason}</strong></p>
                {r.note && <p className="mt-1 text-sm text-foreground">“{r.note}”</p>}
                <p className="mt-2 text-xs text-taupe">
                  Status: {r.status} · {new Date(r.createdAt).toLocaleString()}
                </p>
              </div>
              {r.status === "PENDING" && (
                <div className="flex flex-wrap gap-2">
                  <Button size="sm" variant="outline"
                    onClick={async () => { await resolveReport(r.id, "DISMISSED"); load(); }}>
                    Dispensar
                  </Button>
                  <Button size="sm" variant="outline"
                    onClick={async () => { await setPublic(r.docId, false); await resolveReport(r.id, "RESOLVED"); load(); }}>
                    Retirar do público
                  </Button>
                  <Button size="sm" className="bg-vinho text-vinho-foreground"
                    onClick={async () => { await remove(r.docId); await resolveReport(r.id, "RESOLVED"); load(); }}>
                    Excluir documento
                  </Button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
