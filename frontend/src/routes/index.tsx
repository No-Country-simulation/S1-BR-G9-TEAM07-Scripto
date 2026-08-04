import { createFileRoute, Link } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { ArrowRight, Sparkles, Search, Layers, PlayCircle } from "lucide-react";
import { TopBar } from "@/components/layout/TopBar";
import { OrnamentDivider, AcanthusCorner, ScriptoWordmark } from "@/components/ornaments/Acanthus";
import { useI18n } from "@/lib/i18n";
import { Button } from "@/components/ui/button";
import { useTheme } from "@/lib/theme";



export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "SCRIPTO — Onde suas leituras se organizam" },
      { name: "description", content: "Uma biblioteca inteligente para estudantes organizarem artigos, PDFs e anotações." },
    ],
  }),
  component: Landing,
});

const fadeIn = { hidden: { opacity: 0, y: 24 }, show: { opacity: 1, y: 0, transition: { duration: 0.6 } } };

function Section({ id, children, className = "" }: { id?: string; children: React.ReactNode; className?: string }) {
  return (
    <motion.section
      id={id}
      variants={fadeIn} initial="hidden" whileInView="show" viewport={{ once: true, amount: 0.2 }}
      className={`mx-auto max-w-6xl px-4 py-24 md:px-8 ${className}`}
    >
      {children}
    </motion.section>
  );
}

const teamMembers = [
  {
    name: "Beatriz França Gusmão",
    role: "Full Stack",
    photo: "src/assets/members/beatriz.jpg",
  },
  {
    name: "Daniel Romulo Gomes",
    role: "Back-end",
    photo: "src/assets/members/daniel.jpeg",
  },
  {
    name: "Davenir Ramos",
    role: "Back-end",
    photo: "src/assets/members/davenir.png",
  },
  {
    name: "José Carlos G. Júnior",
    role: "Back-end",
    photo: "src/assets/members/jose.jpeg",
  },
  {
    name: "Juliana Magalhães",
    role: "Data Scientist",
    photo: "src/assets/members/juliana.jpg",
  },
  {
    name: "Maria Gomes",
    role: "Data Scientist",
    photo: "src/assets/members/maria.jpeg",
  },
  {
    name: "Vitor Augusto R. Genesio",
    role: "Back-end",
    photo: "src/assets/members/vitor.jpg",
  },
];

const sponsors = [
  {
    name: "Oracle Next Education",
    logoLight: "src/assets/sponsors/one.png",
    logoDark: "src/assets/sponsors/one.png",
  },
  {
    name: "Oracle",
    logoLight: "src/assets/sponsors/oracle.svg",
    logoDark: "src/assets/sponsors/oracle.svg",
  },
  {
    name: "NoCountry",
    logoLight: "src/assets/sponsors/nocountry.png",
    logoDark: "src/assets/sponsors/nocountry.png",
  },
  {
    name: "Alura",
    logoLight: "src/assets/sponsors/alura.svg",
    logoDark: "src/assets/sponsors/alura.webp",
  }
];

function Landing() {
  const { theme } = useTheme();
  const { t } = useI18n();
  return (
    <div className="relative min-h-screen bg-background text-foreground">

      <TopBar />
      {/* Content that lifts to reveal footer */}
      <main className="relative
        z-10
        min-h-screen
        bg-background
        text-foreground
        duration-300
        md:mb-[83vh]
        shadow-[0_100px_80px_0_var(--color-background)]"
        >

        {/* HERO */}
        <section className="relative overflow-hidden">
          <AcanthusCorner className="pointer-events-none absolute left-4 top-24 h-32 w-32 text-dourado/40" />
          <AcanthusCorner className="pointer-events-none absolute right-4 top-24 h-32 w-32 rotate-90 text-dourado/40" />
          <div className="mx-auto max-w-4xl px-4 py-28 text-center md:py-40">
            <motion.p variants={fadeIn} initial="hidden" animate="show" className="mb-4 inline-flex items-center gap-2 rounded-full border border-bege px-3 py-1 text-xs text-taupe">
              <Sparkles className="h-3 w-3 text-dourado" aria-hidden="true" /> Biblioteca inteligente para estudantes
            </motion.p>
            <motion.h1 variants={fadeIn} initial="hidden" animate="show" className="font-serif text-5xl leading-[1.05] md:text-7xl">
              {t("hero.slogan")}
            </motion.h1>
            <motion.p variants={fadeIn} initial="hidden" animate="show" className="mx-auto mt-6 max-w-2xl text-lg text-taupe">
              {t("hero.subtitle")}
            </motion.p>
            <motion.div variants={fadeIn} initial="hidden" animate="show" className="mt-10 flex flex-wrap justify-center gap-3">
              <Link to="/register">
                <Button size="lg" className="bg-vinho text-vinho-foreground hover:bg-vinho/90">
                  {t("hero.cta")} <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </Link>
              <a href="#how">
                <Button size="lg" variant="outline">{t("cta.secondary")}</Button>
              </a>
            </motion.div>
            <OrnamentDivider className="mt-16" />
          </div>
        </section> {/* FIM HERO */}

        {/* PROBLEM */}
        <Section>
          <div className="grid gap-10 md:grid-cols-2 md:items-center">
            <div>
              <p className="mb-2 text-xs uppercase tracking-widest text-dourado">01 · O problema</p>
              <h2 className="font-serif text-4xl">{t("problem.title")}</h2>
              <p className="mt-4 text-taupe">{t("problem.body")}</p>
            </div>
            <div className="rounded-xl border border-bege bg-muted/40 p-8 font-serif text-2xl italic text-taupe">
              “Onde eu salvei aquele PDF de sociologia mesmo?” <br />“Coloquei aquele artigo em algum lugar…”
            </div>
          </div>
        </Section>

        {/* SOLUTION */}
        <Section className="bg-pessego/20">
          <div className="grid gap-10 md:grid-cols-2 md:items-center">
            <div className="order-2 md:order-1 rounded-xl border border-bege bg-background p-6">
              <div className="space-y-3">
                {["Cole ou envie seu texto", "IA analisa e sugere metadados", "Você acha em segundos"].map((s, i) => (
                  <div key={s} className="flex items-center gap-3 rounded-lg border border-border/60 p-3">
                    <span className="font-display text-2xl text-dourado">0{i + 1}</span>
                    <span className="text-sm">{s}</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="order-1 md:order-2">
              <p className="mb-2 text-xs uppercase tracking-widest text-dourado">02 · A solução</p>
              <h2 className="font-serif text-4xl">{t("solution.title")}</h2>
              <p className="mt-4 text-taupe">{t("solution.body")}</p>
            </div>
          </div>
        </Section>

        {/* HOW */}
        <Section id="how">
          <div className="mx-auto max-w-3xl text-center">
            <p className="mb-2 text-xs uppercase tracking-widest text-dourado">03</p>
            <h2 className="font-serif text-4xl">{t("how.title")}</h2>
            <OrnamentDivider className="mt-6" />
          </div>
          <div className="mt-10 flex aspect-video items-center justify-center rounded-xl border border-bege bg-muted/40 text-taupe">
            <div className="flex items-center gap-3">
              <PlayCircle className="h-10 w-10" aria-hidden="true" />
              <span className="font-serif text-xl">Vídeo demonstrativo em breve</span>
            </div>
          </div>
        </Section>

        {/* FEATURES */}
        <Section id="features" className="bg-muted/40">
          <div className="mx-auto max-w-3xl text-center">
            <p className="mb-2 text-xs uppercase tracking-widest text-dourado">04</p>
            <h2 className="font-serif text-4xl">{t("features.title")}</h2>
          </div>
          <div className="mt-12 grid gap-6 md:grid-cols-3">
            {[
              { Icon: Layers, title: "Categorização automática", body: "Cada texto ganha categoria, nível e tags sugeridas pela análise semântica." },
              { Icon: Search, title: "Encontre em segundos", body: "Filtre por tags, nível, categoria — sua estante inteira, à mão." },
              { Icon: Sparkles, title: "Biblioteca pública", body: "Compartilhe suas melhores leituras e descubra o que outros estudam." },
            ].map(({ Icon, title, body }) => (
              <div key={title} className="relative overflow-hidden rounded-xl border border-bege bg-card p-6">
                <AcanthusCorner className="pointer-events-none absolute -right-2 -top-2 h-16 w-16 text-dourado/30" />
                <Icon className="h-6 w-6 text-vinho" aria-hidden="true" />
                <h3 className="mt-4 font-serif text-xl">{title}</h3>
                <p className="mt-2 text-sm text-taupe">{body}</p>
              </div>
            ))}
          </div>
          <div className="mt-16 text-center">
            <Link to="/register">
              <Button size="lg" className="bg-vinho text-vinho-foreground hover:bg-vinho/90">
                {t("hero.cta")} <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
          </div>
        </Section>

        {/* TEAM */}
        <Section id="team">
          <div className="text-center">
            <p className="mb-6 text-xs uppercase tracking-widest text-dourado">05</p>
            <h2 className="font-serif text-4xl">{t("team.title")}</h2>
          </div>

          <div className="mt-10 flex flex-wrap justify-center gap-6">
            {teamMembers.map((member) => (
              <div
                key={member.name}
                className="w-full max-w-[280px] rounded-xl border border-bege bg-card p-6 text-center"
              >
                <div className="mx-auto mb-4 flex h-20 w-20 items-center justify-center overflow-hidden rounded-full bg-pessego/60">
                  {member.photo ? (
                    <img
                      src={member.photo}
                      alt={member.name}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <span className="font-serif text-xl">
                      {member.name
                        .split(" ")
                        .map((word) => word[0])
                        .join("")}
                    </span>
                  )}
                </div>

                <p className="font-serif text-lg">{member.name}</p>
                <p className="mt-1 text-xs uppercase tracking-wide text-taupe">
                  {member.role}
                </p>
              </div>
            ))}
          </div>
        </Section>

        {/* SPONSORS */}
        <Section className="pb-15">
          <div className="text-center">
            <p className="mb-6 text-xs uppercase tracking-widest text-dourado">06</p>
            <p className="text-xs uppercase tracking-widest text-taupe">
              Apoio institucional
            </p>
          </div>

          <div className="mt-10 flex flex-wrap justify-center gap-6 ">
            {sponsors.map((sponsor) => (
              <div
                key={sponsor.name}
                className="w-full max-w-[220px] rounded-xl border border-dashed border-bege bg-card p-6 text-center transition-all hover:border-dourado hover:shadow-md "
              >
                <div className="flex h-20 items-center justify-center">
                  <img
                    src={theme === "dark" ? sponsor.logoDark : sponsor.logoLight}
                    alt={sponsor.name}
                    className="max-h-16 max-w-full object-contain"
                  />

                </div>

                <p className="mt-4 font-serif text-base">
                  {sponsor.name}
                </p>
              </div>
            ))}
          </div>
        </Section>
        
      </main>
      

      {/* O conteúdo principal precisa ter margin-bottom igual à altura do footer.
      Isso cria o espaço de rolagem necessário para revelar o rodapé fixo. */}


      {/* Giant footer — revealed by scroll */}
      <footer
        className="
          relative
          z-0
          min-h-screen
          overflow-hidden
          border-
          bg-background
          text-foreground
          transition-colors
          duration-300
          md:fixed
          md:inset-x-0
          md:bottom-0
          md:h-[70vh]
          md:min-h-0


          
          "
      >
        <div
          className="
            mx-auto
            flex
            min-h-screen
            max-w-7xl
            flex-col
            border-t
            justify-between
            px-6
            py-16
            text-foreground
            md:h-full
            md:min-h-0
            md:px-16
            "
        >
          <div>
            <ScriptoWordmark className="text-4xl text-primary md:text-6xl" />

            <p className="mt-6 max-w-xl font-serif text-2xl leading-snug md:text-4xl">
              {t("footer.tag")}
            </p>
          </div>

          <div className="mt-16 grid gap-10 sm:grid-cols-2 md:grid-cols-4">
            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">
                Produto
              </p>

              <ul className="space-y-2 text-sm">
                <li>
                  <a
                    href="#features"
                    className="transition-opacity hover:opacity-70"
                  >
                    Funcionalidades
                  </a>
                </li>

                <li>
                  <a href="#how" className="transition-opacity hover:opacity-70">
                    Como funciona
                  </a>
                </li>

                <li>
                  <Link
                    to="/register"
                    className="transition-opacity hover:opacity-70"
                  >
                    Comece agora
                  </Link>
                </li>
              </ul>
            </div>

            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">
                Empresa
              </p>

              <ul className="space-y-2 text-sm">
                <li>
                  <a href="#team" className="transition-opacity hover:opacity-70">
                    Equipe
                  </a>
                </li>

                <li>
                  <a href="#" className="transition-opacity hover:opacity-70">
                    Contato
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">
                Legal
              </p>

              <ul className="space-y-2 text-sm">
                <li>
                  <a href="#" className="transition-opacity hover:opacity-70">
                    Termos
                  </a>
                </li>

                <li>
                  <a href="#" className="transition-opacity hover:opacity-70">
                    Privacidade
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <p className="mb-3 text-xs uppercase tracking-widest text-dourado">
                Moderação
              </p>

              <ul className="space-y-2 text-sm">
                <li>
                  <Link
                    to="/admin/login"
                    className="transition-opacity hover:opacity-70"
                  >
                    Painel admin
                  </Link>
                </li>
              </ul>
            </div>
          </div>

          <div className="mt-12 border-t border-pessego/20 pt-6 text-xs text-foreground/70">
            © {new Date().getFullYear()} SCRIPTO. Todos os direitos reservados.
          </div>
        </div>
      </footer>


    </div>
  );
}