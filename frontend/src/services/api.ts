import axios from "axios";

/**
 * Instância Axios pronta para a futura integração com a API Java Spring Boot.
 * Enquanto isso, todos os services usam dados mockados em memória / localStorage.
 */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api",
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("scripto-token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
