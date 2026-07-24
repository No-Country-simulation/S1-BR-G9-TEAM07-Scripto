import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { OrnamentDivider } from "@/components/ornaments/Acanthus";
import { remove, setPublic, type Doc } from "@/services/documents.service";

export const Route = createFileRoute("/admin/documents")({ component: AdminDocs });

function AdminDocs() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const load = () => setDocs(JSON.parse(localStorage.getItem("scripto-docs") || "[]"));
  useEffect(() => { load(); }, []);

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <p className="text-xs uppercase tracking-widest text-dourado">Gestão</p>
      <h1 className="font-serif text-4xl">Documentos</h1>
      <OrnamentDivider className="mt-6" />
      <div className="mt-6 overflow-hidden rounded-xl border border-bege">
        <table className="w-full text-sm">
          <thead className="bg-muted text-taupe">
            <tr>
              <th className="p-3 text-left">Título</th><th className="p-3 text-left">Autor</th>
              <th className="p-3 text-left">Status</th><th className="p-3 text-left">Público</th>
              <th className="p-3 text-right">Ações</th>
            </tr>
          </thead>
          <tbody>
            {docs.length === 0 && <tr><td colSpan={5} className="p-6 text-center text-taupe">Sem documentos.</td></tr>}
            {docs.map(d => (
              <tr key={d.id} className="border-t border-border">
                <td className="p-3 font-medium">{d.title}</td>
                <td className="p-3 text-taupe">{d.ownerName}</td>
                <td className="p-3">{d.status}</td>
                <td className="p-3">{d.public ? "Sim" : "Não"}</td>
                <td className="p-3 space-x-2 text-right">
                  {d.public && (
                    <Button size="sm" variant="outline"
                      onClick={async () => { await setPublic(d.id, false); load(); }}>Retirar do público</Button>
                  )}
                  <Button size="sm" className="bg-vinho text-vinho-foreground"
                    onClick={async () => { await remove(d.id); load(); }}>Excluir</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
