import { sleep } from "./api";

export type User = {
  id: string;
  name: string;
  email: string;
  cpf: string; // apenas dígitos
  role: "user" | "admin";
  deletionScheduledAt?: string | null;
};

const USERS_KEY = "scripto-users";
const CURRENT_KEY = "scripto-current-user";

function readUsers(): User[] {
  if (typeof window === "undefined") return [];
  try { return JSON.parse(localStorage.getItem(USERS_KEY) || "[]"); } catch { return []; }
}
function writeUsers(u: User[]) { localStorage.setItem(USERS_KEY, JSON.stringify(u)); }

export async function register(input: { name: string; email: string; cpf: string; password: string }) {
  await sleep(500);
  const users = readUsers();
  if (users.find(u => u.email === input.email)) throw new Error("E-mail já cadastrado.");
  const user: User = {
    id: crypto.randomUUID(), name: input.name, email: input.email,
    cpf: input.cpf.replace(/\D/g, ""), role: "user", deletionScheduledAt: null,
  };
  users.push(user); writeUsers(users);
  localStorage.setItem(CURRENT_KEY, JSON.stringify(user));
  localStorage.setItem("scripto-token", "mock-token-" + user.id);
  return user;
}

export async function login(email: string, _password: string) {
  await sleep(400);
  const users = readUsers();
  const user = users.find(u => u.email === email);
  if (!user) throw new Error("Credenciais inválidas.");
  localStorage.setItem(CURRENT_KEY, JSON.stringify(user));
  localStorage.setItem("scripto-token", "mock-token-" + user.id);
  return user;
}

export async function adminLogin(email: string, _password: string) {
  await sleep(400);
  const admin: User = { id: "admin-1", name: "Moderador Scripto", email, cpf: "00000000000", role: "admin" };
  localStorage.setItem(CURRENT_KEY, JSON.stringify(admin));
  localStorage.setItem("scripto-token", "mock-admin-token");
  return admin;
}

export function currentUser(): User | null {
  if (typeof window === "undefined") return null;
  try { return JSON.parse(localStorage.getItem(CURRENT_KEY) || "null"); } catch { return null; }
}

export function logout() {
  localStorage.removeItem(CURRENT_KEY);
  localStorage.removeItem("scripto-token");
}

export async function scheduleDeletion() {
  const user = currentUser(); if (!user) return;
  user.deletionScheduledAt = new Date().toISOString();
  const users = readUsers().map(u => u.id === user.id ? user : u);
  writeUsers(users);
  localStorage.setItem(CURRENT_KEY, JSON.stringify(user));
}

export async function reactivate() {
  const user = currentUser(); if (!user) return;
  user.deletionScheduledAt = null;
  const users = readUsers().map(u => u.id === user.id ? user : u);
  writeUsers(users);
  localStorage.setItem(CURRENT_KEY, JSON.stringify(user));
}

export function updateProfile(patch: Partial<Pick<User, "name" | "email">>) {
  const user = currentUser(); if (!user) return null;
  const next = { ...user, ...patch };
  const users = readUsers().map(u => u.id === user.id ? next : u);
  writeUsers(users);
  localStorage.setItem(CURRENT_KEY, JSON.stringify(next));
  return next;
}
