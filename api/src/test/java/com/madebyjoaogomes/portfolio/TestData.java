package com.madebyjoaogomes.portfolio;

import com.madebyjoaogomes.portfolio.article.Article;
import com.madebyjoaogomes.portfolio.project.CaseFeature;
import com.madebyjoaogomes.portfolio.project.CaseResult;
import com.madebyjoaogomes.portfolio.project.CaseTestimonial;
import com.madebyjoaogomes.portfolio.project.Project;
import com.madebyjoaogomes.portfolio.project.ProjectDetails;
import com.madebyjoaogomes.portfolio.project.ProjectSeo;
import com.madebyjoaogomes.portfolio.project.ProjectSummary;
import java.time.LocalDate;
import java.util.List;

/** Content fixtures shaped like the site's real projects and articles. */
public final class TestData {

    private TestData() {
    }

    public static ProjectSummary summary() {
        return new ProjectSummary("Problema", "Solução", "Contribuição", "Prova");
    }

    public static ProjectDetails details() {
        return new ProjectDetails(
                "Subtítulo", "Front-end", "1 Mês", "Web App", "Jurídico",
                "Desafio", "/challenge.png", "Desafio alt", "SCOPE // CHALLENGE",
                List.of("Dor 1", "Dor 2"),
                "Estratégia", "/strategy.png", "Estratégia alt", "SCOPE // STRATEGY",
                "/solution.png", "Solução alt", "SCOPE // SOLUTION",
                List.of(new CaseFeature("Feature", "Descrição", "/feature.png"), new CaseFeature("Sem imagem", "Descrição", null)),
                List.of(),
                List.of(new CaseResult("1 Hub", "Operação centralizada")),
                new CaseTestimonial("Ótimo trabalho.", "Cliente", "Advogado", "/cliente.webp", "Retrato", "LinkedIn"),
                "Conclusão");
    }

    public static ProjectSeo seo() {
        return new ProjectSeo("Título | João Gomes", "Descrição SEO", "/og.png", 1536, 1024, "Imagem",
                "Nome", "Headline", "Resumo", List.of("UX/UI", "Front-end"));
    }

    public static Project project(String slug, int order) {
        var project = new Project(order);
        project.update(slug, "Projeto " + slug, "Categoria", "/thumb.png", null, summary(), details(), seo());
        return project;
    }

    public static Article article(String slug) {
        var article = new Article();
        article.update(slug, "Artigo " + slug, "Descrição", "## Olá\n\nTexto em **Markdown**.", List.of("Java", "Spring"),
                null, null, null, null, null, null);
        return article;
    }

    public static Article publishedArticle(String slug, LocalDate publishedOn) {
        var article = article(slug);
        article.update(slug, article.getTitle(), article.getDescription(), article.getBodyMarkdown(), article.getTags(),
                null, null, null, null, publishedOn, null);
        article.publish(publishedOn);
        return article;
    }
}
