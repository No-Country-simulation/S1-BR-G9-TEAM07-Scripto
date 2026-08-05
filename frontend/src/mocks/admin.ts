// TODO(BACKEND): remove these development-only records after administrative list endpoints are available.
export type AdminUserPreview = {
  id: number;
  fullName: string;
  email: string;
  role: "USER" | "ADMIN";
  status: "ACTIVE" | "SUSPENDED";
  documents: number;
  reports: number;
};

export type AdminDocumentPreview = {
  id: number;
  title: string;
  author: string;
  visibility: "PUBLIC" | "PRIVATE";
  status: "PROCESSED" | "PROCESSING";
  reports: number;
};

export const devAdminUsers: AdminUserPreview[] = [
  { id: 101, fullName: "Ana Martins", email: "ana@example.dev", role: "USER", status: "ACTIVE", documents: 12, reports: 0 },
  { id: 102, fullName: "Carlos Lima", email: "carlos@example.dev", role: "USER", status: "SUSPENDED", documents: 4, reports: 2 },
  { id: 103, fullName: "Equipe Scripto", email: "admin@example.dev", role: "ADMIN", status: "ACTIVE", documents: 1, reports: 0 },
];

export const devAdminDocuments: AdminDocumentPreview[] = [
  { id: 201, title: "Introdução a estruturas de dados", author: "Ana Martins", visibility: "PUBLIC", status: "PROCESSED", reports: 0 },
  { id: 202, title: "Notas sobre aprendizado de máquina", author: "Carlos Lima", visibility: "PRIVATE", status: "PROCESSING", reports: 1 },
  { id: 203, title: "Resumo de arquitetura de software", author: "Equipe Scripto", visibility: "PUBLIC", status: "PROCESSED", reports: 0 },
];
