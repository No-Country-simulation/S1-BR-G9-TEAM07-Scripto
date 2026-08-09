import { createFileRoute } from "@tanstack/react-router";
import { LegalPage, type LegalSection } from "@/components/LegalPage";
import { useI18n } from "@/lib/i18n";

export const Route = createFileRoute("/terms")({ component: TermsPage });

function TermsPage() {
  const { lang, t } = useI18n();
  const content: Record<string, { introduction: string; sections: LegalSection[] }> = {
    "pt-BR": {
      introduction: "Ao criar uma conta ou utilizar o Scripto, você concorda com estas condições. Elas protegem seus direitos, a comunidade e o funcionamento responsável da plataforma.",
      sections: [
        { title: "1. Finalidade da plataforma", paragraphs: ["O Scripto é uma ferramenta de organização e descoberta de conteúdos de estudo. Você deve utilizar a plataforma para finalidades legítimas, educacionais e compatíveis com a legislação aplicável."] },
        { title: "2. Conta e segurança", paragraphs: ["Você é responsável por manter suas credenciais em sigilo e por informar dados corretos. Ações realizadas por sua conta serão associadas ao seu perfil até que um acesso indevido seja comunicado."] },
        { title: "3. Documentos e direitos autorais", paragraphs: ["Você continua responsável pelo conteúdo enviado e declara possuir autorização para armazená-lo ou compartilhá-lo. Documentos marcados como públicos podem ser exibidos a outros usuários após os controles de moderação."] },
        { title: "4. Moderação e denúncias", paragraphs: ["Conteúdos públicos podem ser denunciados e revisados. O Scripto poderá retirar um documento da área pública, restringir funcionalidades ou suspender contas em caso de violação destes termos, da lei ou dos direitos de terceiros."] },
        { title: "5. Processamento, vetorização e treinamento interno", paragraphs: ["Ao marcar o consentimento correspondente no envio, você autoriza o Scripto a processar e manter internamente no PostgreSQL/pgvector uma cópia textual do conteúdo, representações vetoriais e resultados de classificação/análise para apoiar o treinamento, a avaliação e a melhoria do modelo interno da aplicação.", "Essa autorização não transforma um documento privado em público. Os dados não serão publicados nem fornecidos publicamente como base de treinamento; o uso descrito é interno e limitado ao desenvolvimento e à melhoria do Scripto."] },
        { title: "6. Exclusão e período de recuperação", paragraphs: ["A exclusão da conta utiliza soft delete e inicia um prazo de recuperação de 30 dias. Durante esse período, a conta poderá ser reativada mediante validação das credenciais. Após o prazo, os dados pessoais seguem as regras de retenção e eliminação aplicáveis.", "Após a exclusão definitiva da conta no MySQL, o conteúdo textual autorizado para treinamento, seus vetores e resultados de análise/classificação podem permanecer no corpus interno sem dados pessoais da conta nem vínculo operacional com o usuário, conforme o consentimento registrado."] },
        { title: "7. Disponibilidade e alterações", paragraphs: ["A plataforma pode receber melhorias, manutenções e ajustes de segurança. Mudanças relevantes nestes termos serão comunicadas de forma adequada."] },
      ],
    },
    en: {
      introduction: "By creating an account or using Scripto, you agree to these conditions. They protect your rights, the community, and the responsible operation of the platform.",
      sections: [
        { title: "1. Platform purpose", paragraphs: ["Scripto is a tool for organizing and discovering study content. You must use the platform for legitimate, educational purposes that comply with applicable law."] },
        { title: "2. Account and security", paragraphs: ["You are responsible for keeping your credentials confidential and providing accurate information. Actions performed through your account remain associated with your profile until unauthorized access is reported."] },
        { title: "3. Documents and copyright", paragraphs: ["You remain responsible for submitted content and declare that you have permission to store or share it. Documents marked public may be shown to other users after moderation controls."] },
        { title: "4. Moderation and reports", paragraphs: ["Public content may be reported and reviewed. Scripto may remove a document from public view, restrict features, or suspend accounts when these terms, the law, or third-party rights are violated."] },
        { title: "5. Processing, vectorization, and internal training", paragraphs: ["When you select the relevant consent during submission, you authorize Scripto to internally process and vectorize the document in PostgreSQL/pgvector to support training, evaluation, and improvement of the application's internal model.", "This authorization does not make a private document public. Data will not be published or made publicly available as a training dataset; the described use is internal and limited to Scripto's development and improvement."] },
        { title: "6. Deletion and recovery period", paragraphs: ["Account deletion uses soft delete and starts a 30-day recovery period. During this period, the account may be reactivated after credential validation. Afterward, personal data follows applicable retention and deletion rules.", "After the account is permanently deleted from MySQL, textual content authorized for training, its vectors, and classification/analysis results may remain in the internal corpus without personal account data or an operational link to the user, according to the recorded consent."] },
        { title: "7. Availability and changes", paragraphs: ["The platform may receive improvements, maintenance, and security updates. Material changes to these terms will be communicated appropriately."] },
      ],
    },
  };
  const selected = content[lang];
  return <LegalPage title={t("legal.terms.title")} introduction={selected.introduction} sections={selected.sections} />;
}
