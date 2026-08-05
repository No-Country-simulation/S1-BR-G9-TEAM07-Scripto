import type { UserViewDTO } from "./user.service";

export const TOKEN_STORAGE_KEY = "scripto-token";
export const SESSION_STORAGE_KEY = "scripto-session";

export type SessionAccess = "USER" | "ADMIN";

export type AuthSession = {
  user: UserViewDTO;
  access: SessionAccess;
  expiresAt: number | null;
};

type JwtPayload = {
  exp?: number;
  sub?: string;
};

function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const encodedPayload = token.split(".")[1];
    if (!encodedPayload) return null;
    const normalized = encodedPayload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    return JSON.parse(atob(padded)) as JwtPayload;
  } catch {
    return null;
  }
}

export function tokenExpiresAt(token: string): number | null {
  const exp = decodeJwtPayload(token)?.exp;
  return typeof exp === "number" ? exp * 1000 : null;
}

export function isTokenExpired(token: string, clockSkewMs = 15_000): boolean {
  const expiresAt = tokenExpiresAt(token);
  return expiresAt !== null && expiresAt <= Date.now() + clockSkewMs;
}

export function saveAccessToken(token: string): void {
  if (typeof window !== "undefined") {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }
}

export function saveAuthSession(token: string, session: Omit<AuthSession, "expiresAt">): AuthSession {
  const storedSession: AuthSession = {
    ...session,
    expiresAt: tokenExpiresAt(token),
  };

  if (typeof window !== "undefined") {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(storedSession));
  }

  return storedSession;
}

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (!token) return null;
  if (isTokenExpired(token)) {
    clearAuthSession();
    return null;
  }
  return token;
}

export function currentSession(): AuthSession | null {
  if (typeof window === "undefined" || !getAccessToken()) return null;
  try {
    return JSON.parse(localStorage.getItem(SESSION_STORAGE_KEY) || "null") as AuthSession | null;
  } catch {
    clearAuthSession();
    return null;
  }
}

export function clearAuthSession(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(SESSION_STORAGE_KEY);
}

export function updateStoredSessionUser(user: UserViewDTO): AuthSession | null {
  const session = currentSession();
  const token = getAccessToken();
  if (!session || !token) return null;
  return saveAuthSession(token, { ...session, user });
}
