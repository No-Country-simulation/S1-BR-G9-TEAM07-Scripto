import { sleep } from "./api";
import { currentUser } from "./auth.service";

export type DocStatus = "PENDING" | "PROCESSING" | "PROCESSED" | "ERROR";
export type Level = "Iniciante" | "Intermediário" | "Avançado";

export type Doc = {
  id: string;
  ownerId: string;
  ownerName: string;
  title: string;
  summary: string;
  category: string;
  level: Level;
  tags: string[];
  status: DocStatus;
  attempts: number;
  public: boolean;
  createdAt: string;
  source: "paste" | "file";
};

const KEY = "scripto-docs";

function read(): Doc[] {
  if (typeof window === "undefined") return [];
  try { return JSON.parse(localStorage.getItem(KEY) || "[]"); } catch { return []; }
}
function write(d: Doc[]) { localStorage.setItem(KEY, JSON.stringify(d)); }

const CATEGORIES = ["Filosofia", "História", "Ciência da Computação", "Biologia", "Direito", "Economia", "Literatura", "Matemática"];
const TAG_POOL = ["kant", "algoritmos", "renascença", "estruturas", "ética", "clean-code", "genética", "microeconomia", "modernismo", "cálculo", "ai", "leitura crítica"];
const LEVELS: Level[] = ["Iniciante", "Intermediário", "Avançado"];

function pick<T>(arr: T[], n = 1): T[] {
  const c = [...arr]; const out: T[] = [];
  while (out.length < n && c.length) out.push(c.splice(Math.floor(Math.random() * c.length), 1)[0]);
  return out;
}

export async function listMine(): Promise<Doc[]> {
  await sleep(200);
  const user = currentUser();
  return read().filter(d => d.ownerId === user?.id).sort((a, b) => b.createdAt.localeCompare(a.createdAt));
}

export async function listPublic(): Promise<Doc[]> {
  await sleep(200);
  const user = currentUser();
  return read().filter(d => d.public && d.status === "PROCESSED" && d.ownerId !== user?.id)
    .sort((a, b) => b.createdAt.localeCompare(a.createdAt));
}

export async function create(input: { title: string; content?: string; fileName?: string; source: "paste" | "file"; }): Promise<Doc> {
  await sleep(300);
  const user = currentUser();
  if (!user) throw new Error("Não autenticado.");
  const docs = read();
  if (docs.some(d => d.ownerId === user.id && d.title.trim().toLowerCase() === input.title.trim().toLowerCase())) {
    const err = new Error("Já existe um documento com este título.") as Error & { status?: number };
    err.status = 409;
    throw err;
  }
  const doc: Doc = {
    id: crypto.randomUUID(),
    ownerId: user.id,
    ownerName: user.name,
    title: input.title.trim(),
    summary: "Análise em andamento…",
    category: "—",
    level: "Intermediário",
    tags: [],
    status: "PENDING",
    attempts: 0,
    public: true,
    createdAt: new Date().toISOString(),
    source: input.source,
  };
  docs.push(doc); write(docs);
  // simulate processing pipeline
  processInBackground(doc.id);
  return doc;
}

function processInBackground(id: string) {
  setTimeout(() => updateStatus(id, "PROCESSING"), 800);
  setTimeout(() => finishProcessing(id), 3500);
}

function updateStatus(id: string, status: DocStatus) {
  const docs = read();
  const d = docs.find(x => x.id === id); if (!d) return;
  d.status = status; write(docs);
}

function finishProcessing(id: string) {
  const docs = read();
  const d = docs.find(x => x.id === id); if (!d) return;
  if (Math.random() < 0.15 && d.attempts < 3) {
    d.status = "ERROR"; d.attempts += 1;
  } else {
    d.status = "PROCESSED";
    d.summary = "Resumo automático gerado com base no conteúdo enviado, destacando ideias centrais e conclusões.";
    d.category = pick(CATEGORIES, 1)[0];
    d.level = pick(LEVELS, 1)[0];
    d.tags = pick(TAG_POOL, 5);
  }
  write(docs);
}

export async function retry(id: string) {
  await sleep(200);
  const docs = read();
  const d = docs.find(x => x.id === id); if (!d) throw new Error("Não encontrado");
  if (d.attempts >= 3) throw new Error("Limite de tentativas atingido");
  d.status = "PENDING"; write(docs);
  processInBackground(id);
}

export async function rename(id: string, title: string) {
  await sleep(150);
  const docs = read();
  const d = docs.find(x => x.id === id); if (!d) throw new Error("Não encontrado");
  if (docs.some(x => x.ownerId === d.ownerId && x.id !== id && x.title.trim().toLowerCase() === title.trim().toLowerCase())) {
    const err = new Error("Título já existente.") as Error & { status?: number };
    err.status = 409; throw err;
  }
  d.title = title.trim(); write(docs);
  return d;
}

export async function remove(id: string) {
  await sleep(150);
  write(read().filter(d => d.id !== id));
}

export async function setPublic(id: string, isPublic: boolean) {
  await sleep(100);
  const docs = read();
  const d = docs.find(x => x.id === id); if (!d) return;
  d.public = isPublic; write(docs);
}

export async function recommendations(): Promise<Doc[]> {
  const mine = await listMine();
  const all = await listPublic();
  if (!mine.length) return all.slice(0, 3);
  const myTags = new Set(mine.flatMap(d => d.tags));
  return all
    .map(d => ({ d, score: d.tags.filter(t => myTags.has(t)).length }))
    .sort((a, b) => b.score - a.score)
    .slice(0, 6)
    .map(x => x.d);
}
