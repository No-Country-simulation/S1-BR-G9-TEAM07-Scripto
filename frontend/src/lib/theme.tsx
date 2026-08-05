import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

type Theme = "light" | "dark";
type ThemeContextValue = { theme: Theme; toggle: () => void; setTheme: (theme: Theme) => void };

const ThemeContext = createContext<ThemeContextValue>({ theme: "light", toggle: () => undefined, setTheme: () => undefined });

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>("light");
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    const stored = typeof window !== "undefined" ? localStorage.getItem("scripto-theme") : null;
    const systemDark = typeof window !== "undefined" && window.matchMedia?.("(prefers-color-scheme: dark)").matches;
    setTheme(stored === "light" || stored === "dark" ? stored : systemDark ? "dark" : "light");
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated || typeof document === "undefined") return;
    document.documentElement.classList.toggle("dark", theme === "dark");
    document.documentElement.style.colorScheme = theme;
    localStorage.setItem("scripto-theme", theme);
  }, [theme, hydrated]);

  const value = useMemo(
    () => ({ theme, toggle: () => setTheme((current) => (current === "dark" ? "light" : "dark")), setTheme }),
    [theme],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export const useTheme = () => useContext(ThemeContext);
