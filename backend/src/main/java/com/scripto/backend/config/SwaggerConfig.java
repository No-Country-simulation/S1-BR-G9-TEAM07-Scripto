package com.scripto.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Servidor atual")
                ))
                .info(new Info()
                        .title("Scripto API")
                        .description("""
                                API oficial da plataforma **Scripto**, responsável por organizar,
                                classificar e recomendar conteúdos textuais.

                                ### Módulos disponíveis
                                - Autenticação e gerenciamento de usuários
                                - Documentos e biblioteca pessoal
                                - Classificação e resumos com IA
                                - Recomendações e exploração de conteúdos
                                - Denúncias, moderação e administração

                                Para endpoints protegidos, clique em **Authorize** e informe somente o token JWT.
                                O prefixo `Bearer` é adicionado automaticamente pela interface.
                                """)
                        .version("2.1.0")
                        .contact(new Contact()
                                .name("Equipe Scripto")
                                .email("scripto.team07@outlook.com")
                                .url("https://github.com/No-Country-simulation/S1-BR-G9-TEAM07-Scripto"))
                        .license(new License()
                                .name("MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório do projeto no GitHub")
                        .url("https://github.com/No-Country-simulation/S1-BR-G9-TEAM07-Scripto"))
                .tags(List.of(
                        new Tag().name("Autenticação").description("Cadastro, login e reativação de conta"),
                        new Tag().name("Usuários").description("Consulta, atualização e exclusão de usuários"),
                        new Tag().name("Documentos").description("Envio, consulta e filtragem de documentos"),
                        new Tag().name("Resumos").description("Geração de resumos por inteligência artificial"),
                        new Tag().name("Recomendações").description("Conteúdos semelhantes e recomendações personalizadas"),
                        new Tag().name("Explorar").description("Descoberta de conteúdos públicos"),
                        new Tag().name("Denúncias").description("Envio de denúncias de documentos"),
                        new Tag().name("Administração - Denúncias").description("Revisão administrativa de denúncias"),
                        new Tag().name("Administração - Treinamento").description("Curadoria de candidatos para treinamento")
                ))
                .components(new Components()
                        .addSecuritySchemes(
                                SecurityConfigurations.SECURITY,
                                new SecurityScheme()
                                        .name("Authorization")
                                        .description("Informe o token JWT retornado no login, sem escrever 'Bearer'.")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
