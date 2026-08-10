import { createFileRoute } from "@tanstack/react-router";
import { LegalPage, type LegalSection } from "@/components/LegalPage";
import { useI18n } from "@/lib/i18n";

export const Route = createFileRoute("/privacity")({ component: PrivacyPage });

function PrivacyPage() {
  const { lang, t } = useI18n();
  const content: Record<string, { introduction: string; sections: LegalSection[] }> = {
    "pt-BR": {
      introduction: "Esta política explica quais dados o Scripto trata, para quais finalidades e quais escolhas estão disponíveis para você.",
      sections: [
        { title: "1. Dados coletados", paragraphs: ["Podemos tratar nome, CPF, e-mail, credenciais protegidas, registros de sessão, preferências de idioma e tema, documentos enviados, metadados de classificação, denúncias e informações técnicas necessárias à segurança e ao funcionamento do serviço."] },
        { title: "2. Finalidades", paragraphs: ["Os dados são utilizados para autenticação, organização da biblioteca, processamento de documentos, recomendações, moderação, prevenção a abuso, suporte e melhoria da experiência."] },
        { title: "3. Documentos privados e públicos", paragraphs: ["Documentos privados permanecem restritos à conta, salvo processamento técnico necessário ao serviço e consentimentos específicos. Documentos públicos podem ser exibidos à comunidade com autoria e metadados adequados, sem exposição de CPF ou credenciais."] },
        { title: "4. Processamento para recursos inteligentes", paragraphs: ["Quando houver consentimento para uso interno, o Scripto pode manter uma cópia textual do documento e resultados derivados de análise para oferecer busca, classificação, recomendações e aprimoramento dos recursos da plataforma.", "Essas informações não são disponibilizadas publicamente como conjunto de treinamento. O acesso é limitado às finalidades informadas e às medidas de proteção aplicáveis."] },
        { title: "5. Armazenamento, segurança e retenção", paragraphs: ["Aplicamos controles de segurança compatíveis com o risco. Ao solicitar a exclusão, a conta permanece desativada por 30 dias para permitir reativação. Prazos adicionais podem existir para segurança, cumprimento de obrigações legais e preservação de dados cujo uso interno tenha sido validamente autorizado."] },
        { title: "6. Compartilhamento", paragraphs: ["Não vendemos seus dados pessoais. Algumas funcionalidades podem utilizar fornecedores especializados de infraestrutura ou processamento, sempre de forma limitada à prestação do serviço e sujeita às medidas de proteção aplicáveis."] },
        { title: "7. Seus direitos", paragraphs: ["Você pode solicitar confirmação, acesso, correção, portabilidade quando aplicável, informações sobre compartilhamento, revisão de consentimentos e exclusão, respeitadas as obrigações legais e demais limitações aplicáveis."] },
        { title: "8. Contato", paragraphs: ["Solicitações de privacidade devem ser encaminhadas pelos canais oficiais informados na aplicação."] },
      ],
    },
    en: {
      introduction: "This policy explains which data Scripto processes, for what purposes, and which choices are available to you.",
      sections: [
        { title: "1. Data collected", paragraphs: ["We may process your name, CPF, email, protected credentials, session records, language and theme preferences, submitted documents, classification metadata, reports, and technical information required for security and service operation."] },
        { title: "2. Purposes", paragraphs: ["Data is used for authentication, library organization, document processing, recommendations, moderation, abuse prevention, support, and experience improvements."] },
        { title: "3. Private and public documents", paragraphs: ["Private documents remain restricted to the account, except for technical processing required by the service and specific consents. Public documents may be shown to the community with appropriate authorship and metadata, without exposing CPF or credentials."] },
        { title: "4. Processing for intelligent features", paragraphs: ["When internal-use consent applies, Scripto may retain a textual copy of the document and analysis-derived results to provide search, classification, recommendations, and improvements to platform features.", "This information is not publicly distributed as a training dataset. Access is limited to the stated purposes and applicable safeguards."] },
        { title: "5. Storage, security, and retention", paragraphs: ["We apply security safeguards appropriate to the risk. When deletion is requested, the account remains deactivated for 30 days to allow reactivation. Additional retention periods may apply for security, legal obligations, and data whose internal use was validly authorized."] },
        { title: "6. Sharing", paragraphs: ["We do not sell personal data. Some features may use specialized infrastructure or processing providers, always limited to delivering the service and subject to applicable safeguards."] },
        { title: "7. Your rights", paragraphs: ["You may request confirmation, access, correction, portability when applicable, information about sharing, consent review, and deletion, subject to legal obligations and other applicable limitations."] },
        { title: "8. Contact", paragraphs: ["Privacy requests should be submitted through the official channels shown in the application."] },
      ],
    },
  };
  const selected = content[lang];
  return <LegalPage title={t("legal.privacy.title")} introduction={selected.introduction} sections={selected.sections} />;
}
