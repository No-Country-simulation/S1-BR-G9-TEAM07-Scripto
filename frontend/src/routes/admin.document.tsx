import { createFileRoute, redirect } from "@tanstack/react-router";

export const Route = createFileRoute("/admin/document")({
  beforeLoad: () => {
    throw redirect({ to: "/admin/documents" });
  },
});
