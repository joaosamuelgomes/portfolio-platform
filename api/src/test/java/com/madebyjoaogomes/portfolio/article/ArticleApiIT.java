package com.madebyjoaogomes.portfolio.article;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.madebyjoaogomes.portfolio.IntegrationTest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArticleApiIT extends IntegrationTest {

    private static final String MARKDOWN = "## O problema\n\nTexto com **ênfase**.\n\n```js\nconst a = 1;\n```\n";

    @Test
    void publishedArticlesReachThePublicSnapshotAsRawMarkdown() throws Exception {
        long older = create(request("mais-antigo", LocalDate.of(2026, 1, 10)));
        long newer = create(request("mais-novo", LocalDate.of(2026, 11, 2)));
        create(request("rascunho", null));

        mvc.perform(as(ADMIN, post("/admin/articles/{id}/publish", older))).andExpect(status().isOk());
        mvc.perform(as(ADMIN, post("/admin/articles/{id}/publish", newer))).andExpect(status().isOk());

        mvc.perform(get("/public/content"))
                .andExpect(jsonPath("$.articles", hasSize(2)))
                .andExpect(jsonPath("$.articles[0].slug").value("mais-novo"))
                .andExpect(jsonPath("$.articles[0].publishedAt").value("2026-11-02"))
                .andExpect(jsonPath("$.articles[0].markdown").value(MARKDOWN))
                .andExpect(jsonPath("$.articles[0].tags[0]").value("Arquitetura"))
                .andExpect(jsonPath("$.articles[0].updatedAt").doesNotExist())
                .andExpect(jsonPath("$.articles[0].image").doesNotExist())
                .andExpect(jsonPath("$.articles[1].slug").value("mais-antigo"));
    }

    @Test
    void publishingAnUndatedDraftStampsADate() throws Exception {
        long id = create(request("sem-data", null));

        mvc.perform(as(ADMIN, post("/admin/articles/{id}/publish", id)))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt", notNullValue()));
    }

    @Test
    void unpublishedArticleLeavesTheSnapshot() throws Exception {
        long id = create(request("temporario", LocalDate.of(2026, 3, 1)));
        mvc.perform(as(ADMIN, post("/admin/articles/{id}/publish", id)));
        mvc.perform(as(ADMIN, post("/admin/articles/{id}/unpublish", id)))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mvc.perform(get("/public/content")).andExpect(jsonPath("$.articles", hasSize(0)));
    }

    @Test
    void rejectsH1InTheBody() throws Exception {
        var body = new ArticleRequest("com-h1", "Título", "Descrição", "# Outro título\n\nTexto", List.of(),
                null, null, null, null, null, null);

        mvc.perform(withJson(as(ADMIN, post("/admin/articles")), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("markdownWithoutH1")));
    }

    @Test
    void rejectsImageWithoutSize() throws Exception {
        var body = new ArticleRequest("com-imagem", "Título", "Descrição", MARKDOWN, List.of(),
                "/capa.png", null, null, "Capa", null, null);

        mvc.perform(withJson(as(ADMIN, post("/admin/articles")), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("imageSized")));
    }

    private long create(ArticleRequest request) throws Exception {
        var response = mvc.perform(withJson(as(ADMIN, post("/admin/articles")), request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    private static ArticleRequest request(String slug, LocalDate publishedAt) {
        return new ArticleRequest(slug, "Artigo " + slug, "Descrição", MARKDOWN, List.of("Arquitetura", "Spring Boot"),
                null, null, null, null, publishedAt, null);
    }
}
