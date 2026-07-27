import { api } from "./api";

export type User = {
  id: string;
  fullName: string;
  email: string;
  cpf: string; // apenas dígitos
  role: "user" | "admin";
  deletionScheduledAt?: string | null;
};

const CURRENT_KEY = "scripto-current-user";

export async function register(input: { name: string; email: string; cpf: string; password: string }) {
  try {
    // Backend expects fullName, not name
    const response = await api.post("/user/register", {
      cpf: input.cpf.replace(/\D/g, ""),
      fullName: input.name,
      email: input.email,
      password: input.password,
    });

    // After successful registration, automatically login to get token
    const loginResponse = await login(input.email, input.password);
    return loginResponse;
  } catch (error: any) {
    if (error.response?.status === 400) {
      throw new Error("E-mail já cadastrado.");
    } else if (error.response?.status === 422) {
      const errors = error.response?.data?.errors;
      if (errors?.email) throw new Error(errors.email);
      if (errors?.cpf) throw new Error(errors.cpf);
      if (errors?.password) throw new Error(errors.password);
      throw new Error("Dados inválidos. Verifique os campos.");
    }
    throw new Error("Erro ao criar conta. Tente novamente.");
  }
}

export async function login(email: string, password: string) {
  try {
    const response = await api.post("/user/login", {
      email,
      password,
    });

    const token = response.data.token;
    if (!token) throw new Error("Token não retornado pelo servidor.");
    localStorage.setItem("scripto-token", token);

    // Store user info temporarily (will be fetched from backend in future)
    // For now, we'll create a minimal user object from the email
    const user: User = {
      id: "temp-id", // Will be replaced with actual ID from backend
      fullName: "Usuário", // Will be fetched from backend
      email,
      cpf: "", // Will be fetched from backend
      role: "user",
      deletionScheduledAt: null,
    };

    localStorage.setItem(CURRENT_KEY, JSON.stringify(user));
    return user;
  } catch (error: any) {
    if (error.response?.status === 401) {
      throw new Error("Credenciais inválidas.");
    } else if (error.response?.status === 422) {
      throw new Error("Dados inválidos.");
    }
    throw new Error("Erro ao fazer login. Tente novamente.");
  }
}

export async function adminLogin(email: string, _password: string) {
  // Admin login still uses mock for now - backend doesn't have admin endpoint
  const admin: User = { id: "admin-1", fullName: "Moderador Scripto", email, cpf: "00000000000", role: "admin" };
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

// These functions still use localStorage for now - backend endpoints not implemented yet
export async function scheduleDeletion() {
  const user = currentUser(); if (!user) return;
  user.deletionScheduledAt = new Date().toISOString();
  localStorage.setItem(CURRENT_KEY, JSON.stringify(user));
}

export async function reactivate() {
  const user = currentUser(); if (!user) return;
  user.deletionScheduledAt = null;
  localStorage.setItem(CURRENT_KEY, JSON.stringify(user));
}

export function updateProfile(patch: Partial<Pick<User, "fullName" | "email">>) {
  const user = currentUser(); if (!user) return null;
  const next = { ...user, ...patch };
  localStorage.setItem(CURRENT_KEY, JSON.stringify(next));
  return next;
}
