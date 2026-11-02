package com.madebyjoaogomes.portfolio.web;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

@Configuration
public class OpenApiConfig {

    private static final String PROBLEM_SCHEMA = "ProblemDetail";

    @Bean
    OpenAPI portfolioOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio API")
                        .version("v1")
                        .description("Conteúdo do portfólio madebyjoaogomes.com: cadastro no painel admin e snapshot público para o build do site."))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    /**
     * Documents the problem-detail responses every operation can return, so a generated client
     * gets typed errors instead of {@code unknown}.
     */
    @Bean
    OpenApiCustomizer problemResponsesCustomizer() {
        return openApi -> {
            ModelConverters.getInstance().readAll(ProblemDetail.class)
                    .forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));
            openApi.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                if (path.startsWith("/admin/")) {
                    addProblem(operation, "401", "Token ausente ou inválido");
                    addProblem(operation, "403", "Perfil sem permissão para esta operação");
                }
                if (path.contains("{id}")) {
                    addProblem(operation, "404", "Conteúdo não encontrado");
                }
                if (path.endsWith("/order")) {
                    addProblem(operation, "422", "A ordem não lista cada projeto exatamente uma vez");
                }
                if (path.equals("/auth/token")) {
                    addProblem(operation, "401", "Usuário ou senha incorretos");
                }
                if (operation.getRequestBody() != null) {
                    addProblem(operation, "400", "Requisição inválida; veja a propriedade errors");
                    if (path.startsWith("/admin/") && (method == PathItem.HttpMethod.POST || path.endsWith("{id}"))) {
                        addProblem(operation, "409", "Slug em uso");
                    }
                }
            }));
        };
    }

    private static void addProblem(Operation operation, String status, String description) {
        operation.getResponses().addApiResponse(status, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/problem+json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + PROBLEM_SCHEMA)))));
    }
}
