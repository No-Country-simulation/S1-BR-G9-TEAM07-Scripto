import axios, { AxiosError, type AxiosRequestConfig } from "axios";
import { clearAuthSession, getAccessToken } from "./session";

export type ErrorResponse = {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  fields?: Record<string, string>;
  traceId?: string;
};

export class ApiError extends Error {
  readonly status: number;
  readonly response?: ErrorResponse;

  constructor(message: string, status: number, response?: ErrorResponse) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.response = response;
  }
}

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const apiBaseUrl = configuredBaseUrl || "/api";

export const api = axios.create({
  baseURL: apiBaseUrl,
  // O backend pode fazer até 2 tentativas do Nemotron com read timeout de 45s cada.
  // O cliente precisa esperar mais que a janela máxima para não reportar falha enquanto a API ainda conclui.
  timeout: 120_000,
  headers: { "Content-Type": "application/json" },
});

const PUBLIC_ROUTES = new Set([
  "/user/login",
  "/user/register",
  "/user/reactivate",
  "/user/suspended/password",
]);

function normalizeRequestPath(url?: string): string {
  if (!url) return "";

  try {
    return new URL(url, "http://localhost").pathname.replace(/\/+$/, "");
  } catch {
    return url.split("?")[0].replace(/\/+$/, "");
  }
}

api.interceptors.request.use((config) => {
  const requestPath = normalizeRequestPath(config.url);

  // Rotas públicas não devem receber Authorization. Isso evita que um JWT
  // antigo ou inválido bloqueie login, cadastro ou reativação no SecurityFilter.
  if (PUBLIC_ROUTES.has(requestPath)) {
    if (typeof config.headers.delete === "function") {
      config.headers.delete("Authorization");
    } else {
      delete config.headers.Authorization;
    }

    return config;
  }

  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;

  return config;
});

function normalizeError(error: AxiosError<unknown>): ApiError {
  const status = error.response?.status ?? 0;
  const raw = error.response?.data;

  if (raw && typeof raw === "object" && "status" in raw) {
    const response = raw as ErrorResponse;
    return new ApiError(response.message || response.error || error.message, status || response.status, response);
  }

  if (typeof raw === "string" && raw.trim()) {
    return new ApiError(raw, status);
  }

  if (!error.response) {
    return new ApiError("Não foi possível conectar à API do Scripto.", 0);
  }

  const fallbackMessages: Record<number, string> = {
    400: "Requisição inválida.",
    401: "E-mail, senha ou token inválido.",
    403: "Acesso negado para este recurso.",
    404: "Recurso não encontrado.",
    409: "A operação conflita com o estado atual do recurso.",
    410: "O prazo para reativação desta conta expirou.",
    423: "Esta conta está bloqueada administrativamente.",
    422: "Um ou mais campos estão inválidos.",
    429: "Limite de requisições atingido. Tente novamente mais tarde.",
    500: "Erro interno da API.",
    503: "Serviço temporariamente indisponível.",
  };

  return new ApiError(fallbackMessages[status] || `A API respondeu com status ${status}.`, status);
}

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<unknown>) => {
    const normalized = normalizeError(error);
    if (normalized.status === 401) {
      clearAuthSession();
      if (typeof window !== "undefined") {
        window.dispatchEvent(new CustomEvent("scripto:unauthorized"));
      }
    }
    return Promise.reject(normalized);
  },
);

export async function apiRequest<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await api.request<T>(config);
  return response.data;
}

export function fieldError(error: unknown, field: string): string | undefined {
  return error instanceof ApiError ? error.response?.fields?.[field] : undefined;
}
