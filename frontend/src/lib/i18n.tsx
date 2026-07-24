import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

type Lang = "pt-BR" | "en";

const dict = {
  "pt-BR": {
    "nav.features": "Funcionalidades",
    "nav.how": "Como funciona",
    "nav.team": "Equipe",
    "nav.login": "Entrar",
    "nav.register": "Cadastrar",
    "hero.slogan": "Onde será que eu deixei? Está no Scripto!",
    "hero.subtitle": "A biblioteca inteligente para estudantes organizarem artigos, PDFs e anotações em segundos.",
    "hero.cta": "Comece a organizar",
    "problem.title": "Textos por toda parte, memória em nenhuma",
    "problem.body": "Estudantes acumulam artigos, PDFs e trechos colados em dezenas de lugares. Encontrar de novo é caça ao tesouro.",
    "solution.title": "Uma estante inteligente para tudo que você lê",
    "solution.body": "O Scripto analisa cada texto, sugere categoria, nível e tags — e devolve tudo organizado.",
    "how.title": "Como funciona",
    "features.title": "O que você pode fazer",
    "team.title": "Equipe",
    "sponsors.title": "Apoio",
    "footer.tag": "Onde suas leituras se organizam.",
    "cta.secondary": "Ver biblioteca pública",
    "auth.login.title": "Bem-vindo de volta",
    "auth.login.subtitle": "Entre para acessar sua biblioteca.",
    "auth.register.title": "Crie sua conta",
    "auth.register.subtitle": "Comece a organizar suas leituras em minutos.",
    "auth.email": "E-mail",
    "auth.password": "Senha",
    "auth.confirm": "Confirmar senha",
    "auth.name": "Nome completo",
    "auth.cpf": "CPF",
    "auth.terms": "Li e aceito os termos de uso e a política de privacidade",
    "auth.submit.login": "Entrar",
    "auth.submit.register": "Criar conta",
    "auth.have": "Já tem conta?",
    "auth.no": "Ainda não tem conta?",
    "app.nav.new": "Novo documento",
    "app.nav.library": "Minha biblioteca",
    "app.nav.explore": "Explorar",
    "app.nav.profile": "Perfil",
    "app.nav.logout": "Sair",
  },
  en: {
    "nav.features": "Features",
    "nav.how": "How it works",
    "nav.team": "Team",
    "nav.login": "Sign in",
    "nav.register": "Sign up",
    "hero.slogan": "Where did I leave that? It's on Scripto!",
    "hero.subtitle": "The smart library for students to organize articles, PDFs and notes in seconds.",
    "hero.cta": "Start organizing",
    "problem.title": "Text everywhere, memory nowhere",
    "problem.body": "Students pile up articles, PDFs and pasted snippets across dozens of places. Finding them again is a treasure hunt.",
    "solution.title": "A smart shelf for everything you read",
    "solution.body": "Scripto analyzes each text, suggests a category, level and tags — and gives it back organized.",
    "how.title": "How it works",
    "features.title": "What you can do",
    "team.title": "Team",
    "sponsors.title": "Sponsors",
    "footer.tag": "Where your readings find their place.",
    "cta.secondary": "See public library",
    "auth.login.title": "Welcome back",
    "auth.login.subtitle": "Sign in to open your library.",
    "auth.register.title": "Create your account",
    "auth.register.subtitle": "Start organizing your readings in minutes.",
    "auth.email": "Email",
    "auth.password": "Password",
    "auth.confirm": "Confirm password",
    "auth.name": "Full name",
    "auth.cpf": "CPF",
    "auth.terms": "I have read and accept the terms of use and privacy policy",
    "auth.submit.login": "Sign in",
    "auth.submit.register": "Create account",
    "auth.have": "Already have an account?",
    "auth.no": "Don't have an account yet?",
    "app.nav.new": "New document",
    "app.nav.library": "My library",
    "app.nav.explore": "Explore",
    "app.nav.profile": "Profile",
    "app.nav.logout": "Sign out",
  },
} as const;

type Key = keyof typeof dict["pt-BR"];

const Ctx = createContext<{ lang: Lang; setLang: (l: Lang) => void; t: (k: Key) => string }>({
  lang: "pt-BR",
  setLang: () => {},
  t: (k) => k,
});

export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setLang] = useState<Lang>("pt-BR");
  useEffect(() => {
    const stored = (typeof window !== "undefined" && localStorage.getItem("scripto-lang")) as Lang | null;
    if (stored) setLang(stored);
  }, []);
  useEffect(() => {
    if (typeof window !== "undefined") localStorage.setItem("scripto-lang", lang);
  }, [lang]);
  const t = (k: Key) => (dict[lang] as Record<string, string>)[k] ?? k;
  return <Ctx.Provider value={{ lang, setLang, t }}>{children}</Ctx.Provider>;
}

export const useI18n = () => useContext(Ctx);
