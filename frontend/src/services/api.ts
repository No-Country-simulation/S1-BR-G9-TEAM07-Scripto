import axios, { AxiosError } from "axios";

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const apiBaseUrl = configuredBaseUrl || "http://localhost:8080";

if (!configuredBaseUrl && import.meta.env.DEV) {
  console.warn(
    "VITE_API_BASE_URL não foi configurada. Usando http://localhost:8080 apenas em desenvolvimento.",
  );
}

export const api = axios.create({
  baseURL: apiBaseUrl,
  timeout: 15_000,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("scripto-token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && typeof window !== "undefined") {
      localStorage.removeItem("scripto-token");
      localStorage.removeItem("scripto-current-user");
    }
    return Promise.reject(error);
  },
);

export const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));
