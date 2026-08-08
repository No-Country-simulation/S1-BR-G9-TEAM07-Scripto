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
        { title: "4. Vetorização no PostgreSQL/pgvector", paragraphs: ["Quando houver consentimento explícito no envio, o Scripto pode manter no ambiente interno uma cópia textual do documento, representações vetoriais e resultados de classificação/análise no PostgreSQL/pgvector para busca semântica, avaliação, treinamento e melhoria do modelo interno.", "Esses dados e vetores não são disponibilizados publicamente como conjunto de treinamento. O acesso é limitado ao ambiente interno e às finalidades informadas."] },
        { title: "5. Armazenamento, segurança e retenção", paragraphs: ["Aplicamos controles técnicos e organizacionais compatíveis com o risco. A conta excluída permanece em soft delete por 30 dias para permitir reativação. Prazos adicionais podem existir para segurança, cumprimento legal e integridade técnica do modelo interno quando houver consentimento válido."] },
        { title: "6. Compartilhamento", paragraphs: ["Não vendemos seus dados pessoais. O tratamento pode envolver provedores essenciais de infraestrutura e serviços de IA quando você autorizar o uso externo, sempre limitado à operação contratada e às medidas de proteção aplicáveis."] },
        { title: "7. Seus direitos", paragraphs: ["Você pode solicitar confirmação, acesso, correção, portabilidade quando aplicável, informações sobre compartilhamento, revisão de consentimentos e exclusão, respeitadas obrigações legais e limitações técnicas devidamente justificadas."] },
        { title: "8. Contato", paragraphs: ["Solicitações de privacidade devem ser encaminhadas pelos canais oficiais informados na aplicação ou pela equipe responsável pelo projeto Scripto."] },
      ],
    },
    en: {
      introduction: "This policy explains which data Scripto processes, for what purposes, and which choices are available to you.",
      sections: [
        { title: "1. Data collected", paragraphs: ["We may process your name, CPF, email, protected credentials, session records, language and theme preferences, submitted documents, classification metadata, reports, and technical information required for security and service operation."] },
        { title: "2. Purposes", paragraphs: ["Data is used for authentication, library organization, document processing, recommendations, moderation, abuse prevention, support, and experience improvements."] },
        { title: "3. Private and public documents", paragraphs: ["Private documents remain restricted to the account, except for technical processing required by the service and specific consents. Public documents may be shown to the community with appropriate authorship and metadata, without exposing CPF or credentials."] },
        { title: "4. PostgreSQL/pgvector vectorization", paragraphs: ["With explicit consent at submission time, Scripto may retain an internal textual copy of the document, vector representations, and classification/analysis results in PostgreSQL/pgvector for semantic search, evaluation, training, and improvement of the internal model.", "This data and its vectors are not publicly distributed as a training dataset. Access is limited to the internal environment and the stated purposes."] },
        { title: "5. Storage, security, and retention", paragraphs: ["We apply technical and organizational safeguards appropriate to the risk. A deleted account remains under soft delete for 30 days to allow reactivation. Additional periods may apply for security, legal compliance, and internal model integrity when valid consent exists."] },
        { title: "6. Sharing", paragraphs: ["We do not sell personal data. Processing may involve essential infrastructure and AI service providers when you authorize external use, limited to the contracted operation and applicable safeguards."] },
        { title: "7. Your rights", paragraphs: ["You may request confirmation, access, correction, portability when applicable, information about sharing, consent review, and deletion, subject to legal obligations and duly justified technical limitations."] },
        { title: "8. Contact", paragraphs: ["Privacy requests should be submitted through the official channels shown in the application or to the team responsible for the Scripto project."] },
      ],
    },
  };
  const selected = content[lang];
  return <LegalPage title={t("legal.privacy.title")} introduction={selected.introduction} sections={selected.sections} />;
}
