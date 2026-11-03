package com.madebyjoaogomes.portfolio.project;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.madebyjoaogomes.portfolio.IntegrationTest;
import com.madebyjoaogomes.portfolio.TestData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ProjectApiIT extends IntegrationTest {

    @Test
    void publishedProjectsReachThePublicSnapshotInDisplayOrder() throws Exception {
        long gelatto = create("gelatto-sorvetes");
        long cm = create("cm-advocacia");
        create("rascunho");

        mvc.perform(as(ADMIN, post("/admin/projects/{id}/publish", gelatto))).andExpect(status().isOk());
        mvc.perform(as(ADMIN, post("/admin/projects/{id}/publish", cm)))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mvc.perform(get("/public/content"))
                .andExpect(jsonPath("$.projects", hasSize(2)))
                .andExpect(jsonPath("$.projects[0].slug").value("gelatto-sorvetes"))
                .andExpect(jsonPath("$.projects[0].id").value(gelatto))
                .andExpect(jsonPath("$.projects[0].order").value(1))
                .andExpect(jsonPath("$.projects[0].details.features[0].title").value("Feature"))
                .andExpect(jsonPath("$.projects[0].details.features[1].image").doesNotExist())
                .andExpect(jsonPath("$.projects[0].seo.about[1]").value("Front-end"))
                .andExpect(jsonPath("$.projects[0].logo").doesNotExist())
                .andExpect(jsonPath("$.projects[0].status").doesNotExist())
                .andExpect(jsonPath("$.projects[1].slug").value("cm-advocacia"));

        mvc.perform(withJson(as(ADMIN, put("/admin/projects/order")), new ProjectOrderRequest(List.of(cm, gelatto, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(cm))
                .andExpect(jsonPath("$[0].order").value(1));
        mvc.perform(as(ADMIN, post("/admin/projects/{id}/archive", gelatto))).andExpect(status().isOk());

        mvc.perform(get("/public/content"))
                .andExpect(jsonPath("$.projects", hasSize(1)))
                .andExpect(jsonPath("$.projects[0].slug").value("cm-advocacia"));
    }

    @Test
    void listsFilteredByStatus() throws Exception {
        long published = create("publicado");
        create("rascunho");
        mvc.perform(as(ADMIN, post("/admin/projects/{id}/publish", published)));

        mvc.perform(as(VIEWER, get("/admin/projects").param("status", "DRAFT")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].slug").value("rascunho"));
    }

    @Test
    void updatesAndDeletes() throws Exception {
        long id = create("antigo");

        mvc.perform(withJson(as(ADMIN, put("/admin/projects/{id}", id)), request("novo-slug")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("novo-slug"));
        mvc.perform(as(ADMIN, delete("/admin/projects/{id}", id))).andExpect(status().isNoContent());
        mvc.perform(as(ADMIN, get("/admin/projects/{id}", id)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Projeto %d não encontrado".formatted(id)));
    }

    @Test
    void rejectsInvalidNestedFieldsWithTheirPaths() throws Exception {
        var body = json.valueToTree(request("invalido"));
        ((ObjectNode) body.get("details")).put("heroSubtitle", "");
        ((ObjectNode) body).put("slug", "Slug Inválido");

        mvc.perform(withJson(as(ADMIN, post("/admin/projects")), body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[*].field", hasItem("details.heroSubtitle")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("slug")));
    }

    @Test
    void rejectsImageUrlsWithOtherSchemes() throws Exception {
        var body = json.valueToTree(request("esquema"));
        ((ObjectNode) body).put("thumbnail", "javascript:alert(1)");
        ((ObjectNode) body.get("details").get("features").get(0)).put("image", "//outro-site.com/x.png");
        ((ObjectNode) body.get("seo")).put("image", "data:image/svg+xml,<svg onload=alert(1)>");

        mvc.perform(withJson(as(ADMIN, post("/admin/projects")), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItems(
                        "thumbnail", "details.features[0].image", "seo.image")));
    }

    @Test
    void rejectsDuplicateSlug() throws Exception {
        create("gelatto");

        mvc.perform(withJson(as(ADMIN, post("/admin/projects")), request("gelatto")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Slug em uso"));
    }

    @Test
    void rejectsIncompleteReorder() throws Exception {
        long first = create("a");
        create("b");

        mvc.perform(withJson(as(ADMIN, put("/admin/projects/order")), new ProjectOrderRequest(List.of(first))))
                .andExpect(status().isUnprocessableEntity());
    }

    private long create(String slug) throws Exception {
        var response = mvc.perform(withJson(as(ADMIN, post("/admin/projects")), request(slug)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    private static ProjectRequest request(String slug) {
        return new ProjectRequest(slug, "Projeto " + slug, "Categoria", "/thumb.png", null,
                TestData.summary(), TestData.details(), TestData.seo());
    }
}
