import { createFileRoute, Link } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { ArrowRight, Compass, Layers3, Search, Sparkles } from "lucide-react";
import { TopBar } from "@/components/layout/TopBar";
import { AcanthusCorner, OrnamentDivider, ScriptoWordmark } from "@/components/ornaments/Acanthus";
import { Button } from "@/components/ui/button";
import { useI18n } from "@/lib/i18n";
import { useTheme } from "@/lib/theme";
import beatriz from "@/assets/members/beatriz.jpg";
import daniel from "@/assets/members/daniel.jpeg";
import davenir from "@/assets/members/davenir.png";
import jose from "@/assets/members/jose.jpeg";
import juliana from "@/assets/members/juliana.jpg";
import maria from "@/assets/members/maria.jpeg";
import vitor from "@/assets/members/vitor.jpg";
import oneLogo from "@/assets/sponsors/one.png";
import oracleLogo from "@/assets/sponsors/oracle.svg";
import noCountryLogo from "@/assets/sponsors/nocountry.png";
import aluraLogo from "@/assets/sponsors/alura.svg";
import aluraLogoDark from "@/assets/sponsors/alura.webp";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "SCRIPTO — Onde suas leituras se organizam" },
      { name: "description", content: "Uma biblioteca inteligente para estudantes organizarem artigos, textos e anotações." },
    ],
  }),
  component: LandingPage,
});

const fadeIn = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0, transition: { duration: 0.55 } } };
const DEMO_VIDEO_URL = "https://www.youtube.com/watch?v=4xq5QzNG-m8&list=RD4xq5QzNG-m8&start_radio=1&pp=ygUXYmFkIG9tZW5zIGR5aW5nIHRvIGxvdmWgBwE%3D";

function youtubeEmbedUrl(url: string) {
  try {
    const parsed = new URL(url);
    const id = parsed.hostname.includes("youtu.be") ? parsed.pathname.slice(1) : parsed.searchParams.get("v");
    return id ? `https://www.youtube-nocookie.com/embed/${id}` : url;
  } catch {
    return url;
  }
}

const team = [
  { name: "Beatriz França Gusmão", role: "Full Stack", photo: beatriz },
  { name: "Daniel Romulo Gomes", role: "Back-end", photo: daniel },
  { name: "Davenir Ramos", role: "Back-end", photo: davenir },
  { name: "José Carlos G. Júnior", role: "Back-end", photo: jose },
  { name: "Juliana Magalhães", role: "Data Scientist", photo: juliana },
  { name: "Maria Gomes", role: "Data Scientist", photo: maria },
  { name: "Vitor Augusto R. Genesio", role: "Back-end", photo: vitor },
];
const sponsors = [
  { name: "Oracle Next Education", logoLight: oneLogo, logoDark: oneLogo },
  { name: "Oracle", logoLight: oracleLogo, logoDark: oracleLogo },
  { name: "NoCountry", logoLight: noCountryLogo, logoDark: noCountryLogo },
  { name: "Alura", logoLight: aluraLogo, logoDark: aluraLogoDark },
];

function AnimatedSection({ id, children, className = "" }: { id?: string; children: React.ReactNode; className?: string }) {
  return (
    <motion.section id={id} variants={fadeIn} initial="hidden" whileInView="show" viewport={{ once: true, amount: 0.15 }} className={`px-4 py-20 sm:py-24 ${className}`}>
      <div className="mx-auto max-w-6xl">{children}</div>
    </motion.section>
  );
}

function LandingPage() {
  const { t } = useI18n();
  const { theme } = useTheme();
  const features = [
    { Icon: Layers3, title: t("landing.feature.classification.title"), body: t("landing.feature.classification.body") },
    { Icon: Search, title: t("landing.feature.search.title"), body: t("landing.feature.search.body") },
    { Icon: Compass, title: t("landing.feature.community.title"), body: t("landing.feature.community.body") },
  ];
  const steps = [t("landing.step.1"), t("landing.step.2"), t("landing.step.3")];

  return (
    <div className="relative min-h-screen bg-background text-foreground">
      <TopBar />
      <main
        id="main-content"
        className="relative z-10 min-h-screen bg-background text-foreground shadow-[0_100px_80px_0_var(--color-background)] transition-colors duration-300 md:mb-[83vh]"
      >
        <section className="relative overflow-hidden border-b border-border/60">
          <AcanthusCorner className="pointer-events-none absolute left-0 top-10 h-40 w-40 text-dourado/25 sm:h-64 sm:w-64" />
          <AcanthusCorner className="pointer-events-none absolute right-0 top-10 h-40 w-40 rotate-90 text-dourado/25 sm:h-64 sm:w-64" />
          <div className="relative mx-auto flex min-h-[calc(100vh-5rem)] max-w-5xl flex-col items-center justify-center px-4 py-20 text-center sm:py-28 ">
            <motion.p variants={fadeIn} initial="hidden" animate="show" className="inline-flex items-center gap-2 rounded-full border border-dourado/40 bg-dourado/5 px-3 py-1.5 text-xs font-medium text-muted-foreground">
              <Sparkles className="h-3.5 w-3.5 text-dourado" aria-hidden="true" /> {t("landing.badge")}
            </motion.p>
            <motion.h1 variants={fadeIn} initial="hidden" animate="show" className="mt-6 max-w-4xl text-balance font-serif text-5xl leading-[1.02] sm:text-6xl lg:text-7xl">
              {t("landing.hero.title")}
            </motion.h1>
            <motion.p variants={fadeIn} initial="hidden" animate="show" className="mt-6 max-w-2xl text-balance text-base leading-7 text-muted-foreground sm:text-lg">
              {t("landing.hero.subtitle")}
            </motion.p>
            <motion.div variants={fadeIn} initial="hidden" animate="show" className="mt-9 flex w-full flex-col justify-center gap-3 sm:w-auto sm:flex-row">
              <Link to="/register"><Button size="lg" className="w-full bg-vinho text-vinho-foreground hover:bg-vinho/90 sm:w-auto">{t("landing.hero.primary")} <ArrowRight className="ml-2 h-4 w-4" aria-hidden="true" /></Button></Link>
              <a href="#features"><Button size="lg" variant="outline" className="w-full sm:w-auto">{t("landing.hero.secondary")}</Button></a>
            </motion.div>
            <div className="mt-12 w-full max-w-3xl"><OrnamentDivider /></div>
          </div>
        </section>

        <AnimatedSection>
          <div className="grid gap-8 md:grid-cols-2 md:items-center">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-dourado">{t("landing.problem.eyebrow")}</p>
              <h2 className="mt-2 text-balance font-serif text-4xl sm:text-5xl">{t("landing.problem.title")}</h2>
              <p className="mt-5 leading-7 text-muted-foreground">{t("landing.problem.body")}</p>
            </div>
            <blockquote className="relative overflow-hidden rounded-2xl border border-border bg-muted/35 p-8 font-serif text-3xl italic leading-tight text-muted-foreground hover:-translate-y-2">
              <AcanthusCorner className="pointer-events-none absolute -right-3 -top-3 h-24 w-24 text-dourado/25" />
              “{t("landing.problem.quote")}”
            </blockquote>
          </div>
        </AnimatedSection>

        <AnimatedSection className="border-y border-border/60 bg-muted/25">
          <div className="grid gap-10 md:grid-cols-2 md:items-center">
            <div className="order-2 rounded-2xl border border-border bg-card p-5 shadow-sm md:order-1 sm:p-7">
              <div className="space-y-3">
                {steps.map((step, index) => (
                  <div key={step} className="flex items-center gap-4 rounded-xl border border-border/70 bg-background p-4 hover:-translate-y-1">
                    <span className="font-display text-2xl text-dourado">0{index + 1}</span>
                    <span className="text-sm leading-relaxed">{step}</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="order-1 md:order-2">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-dourado">{t("landing.solution.eyebrow")}</p>
              <h2 className="mt-2 text-balance font-serif text-4xl sm:text-5xl">{t("landing.solution.title")}</h2>
              <p className="mt-5 leading-7 text-muted-foreground">{t("landing.solution.body")}</p>
            </div>
          </div>
        </AnimatedSection>

        <AnimatedSection id="how">
          <div className="text-center">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-dourado">{t("landing.how.eyebrow")}</p>
            <h2 className="mt-2 font-serif text-3xl sm:text-3xl">{t("landing.how.title")}</h2>
          </div>
          <div className="mx-auto mt-10 aspect-video w-full max-w-5xl overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
            <iframe
              className="h-full w-full"
              src={youtubeEmbedUrl(DEMO_VIDEO_URL)}
              title={t("landing.demo")}
              loading="lazy"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              referrerPolicy="strict-origin-when-cross-origin"
              allowFullScreen
            />
          </div>
        </AnimatedSection>

        <AnimatedSection id="features" className="border-y border-border/60 bg-muted/25">
          <div className="text-center">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-dourado">{t("landing.features.eyebrow")}</p>
            <h2 className="mt-2 font-serif text-3xl sm:text-3xl">{t("landing.features.title")}</h2>
          </div>
          <div className="mt-10 grid gap-5 md:grid-cols-3">
            {features.map(({ Icon, title, body }) => (
              <article key={title} className="relative overflow-hidden rounded-2xl border border-border bg-card p-6 shadow-sm transition hover:-translate-y-2 hover:shadow-lg">
                <AcanthusCorner className="pointer-events-none absolute -right-2 -top-2 h-20 w-20 text-dourado/20" />
                <div className="inline-flex rounded-xl bg-vinho/10 p-3 text-vinho"><Icon className="h-5 w-5" aria-hidden="true" /></div>
                <h3 className="mt-5 font-serif text-2xl">{title}</h3>
                <p className="mt-3 text-sm leading-6 text-muted-foreground">{body}</p>
              </article>
            ))}
          </div>
          <div className="mt-10 text-center"><Link to="/register"><Button size="lg" className="bg-vinho text-vinho-foreground hover:bg-vinho/90">{t("landing.hero.primary")} <ArrowRight className="ml-2 h-4 w-4" /></Button></Link></div>
        </AnimatedSection>

        <AnimatedSection id="team">
          <div className="text-center">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-dourado">{t("landing.team.eyebrow")}</p>
            <h2 className="mt-2 font-serif text-3xl sm:text-3xl">{t("landing.team.title")}</h2>
          </div>
          <div className="mt-10 flex flex-wrap justify-center gap-4 sm:gap-5">
            {team.map((member) => (
              <article
                key={member.name}
                className="w-[calc(50%_-_0.5rem)] max-w-[260px] rounded-2xl border border-border bg-card p-4 text-center shadow-sm sm:w-[calc(33.333%_-_0.875rem)] sm:p-5 lg:w-[calc(25%_-_0.9375rem)] hover:-translate-y-2"
              >
                <img src={member.photo} alt="" loading="lazy" className="mx-auto aspect-square w-24 rounded-full object-cover ring-4 ring-pessego/30 sm:w-28" />
                <h3 className="mt-4 font-serif text-lg leading-tight">{member.name}</h3>
                <p className="mt-1 text-[11px] font-medium uppercase tracking-wider text-muted-foreground">{member.role}</p>
              </article>
            ))}
          </div>
        </AnimatedSection>

        <AnimatedSection className="pb-16">
          <div className="text-center">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-dourado">06 · Apoio</p>
            <p className="mt-2 font-serif text-3xl sm:text-3xl">
              {t("landing.sponsors.institutional")}
            </p>
          </div>

          <div className="mt-10 flex flex-wrap justify-center gap-6">
            {sponsors.map((sponsor) => (
              <div
                key={sponsor.name}
                className="w-full max-w-[220px] rounded-xl border border-dashed border-bege bg-card p-6 text-center transition-all hover:border-dourado hover:shadow-md hover:-translate-y-2"
              >
                <div className="flex h-20 items-center justify-center">
                  <img
                    src={theme === "dark" ? sponsor.logoDark : sponsor.logoLight}
                    alt={sponsor.name}
                    loading="lazy"
                    className="max-h-16 max-w-full object-contain"
                  />
                </div>
                <p className="mt-4 font-serif text-base">{sponsor.name}</p>
              </div>
            ))}
          </div>
        </AnimatedSection>
      </main>

      <footer className="relative z-0 min-h-screen overflow-hidden bg-background text-foreground transition-colors duration-300 md:fixed md:inset-x-0 md:bottom-0 md:h-[70vh] md:min-h-0">
        <div className="mx-auto flex min-h-screen max-w-7xl flex-col justify-between border-t border-border px-6 py-16 text-foreground md:h-full md:min-h-0 md:px-16">
          <div>
            <ScriptoWordmark className="text-4xl text-primary md:text-6xl" />
            <p className="mt-6 max-w-xl font-serif text-2xl leading-snug md:text-4xl">
              {t("landing.footer.tag")}
            </p>
          </div>

          <div className="mt-16 grid gap-10 sm:grid-cols-2 md:grid-cols-4">
            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">{t("landing.footer.product")}</p>
              <ul className="space-y-2 text-sm">
                <li><a href="#features" className="transition-opacity hover:opacity-70">{t("nav.features")}</a></li>
                <li><a href="#how" className="transition-opacity hover:opacity-70">{t("nav.how")}</a></li>
                <li><Link to="/register" className="transition-opacity hover:opacity-70">{t("landing.footer.start")}</Link></li>
              </ul>
            </div>

            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">{t("landing.footer.company")}</p>
              <ul className="space-y-2 text-sm">
                <li><a href="#team" className="transition-opacity hover:opacity-70">{t("nav.team")}</a></li>
                <li><Link to="/login" className="transition-opacity hover:opacity-70">{t("nav.login")}</Link></li>
              </ul>
            </div>

            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">{t("landing.footer.legal")}</p>
              <ul className="space-y-2 text-sm">
                <li><Link to="/terms" className="transition-opacity hover:opacity-70">{t("nav.terms")}</Link></li>
                <li><Link to="/privacity" className="transition-opacity hover:opacity-70">{t("nav.privacy")}</Link></li>
              </ul>
            </div>

            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">{t("landing.footer.moderation")}</p>
              <ul className="space-y-2 text-sm">
                <li><Link to="/admin/login" className="transition-opacity hover:opacity-70">{t("landing.footer.admin")}</Link></li>
              </ul>
            </div>
          </div>

          <div className="mt-12 border-t border-border pt-6 text-xs text-foreground/70">
            © {new Date().getFullYear()} SCRIPTO. {t("landing.footer.rights")}
          </div>
        </div>
      </footer>
    </div>
  );
}
