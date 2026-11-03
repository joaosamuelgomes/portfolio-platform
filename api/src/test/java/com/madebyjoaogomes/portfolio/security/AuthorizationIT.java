package com.madebyjoaogomes.portfolio.security;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.madebyjoaogomes.portfolio.IntegrationTest;
import com.madebyjoaogomes.portfolio.TestData;
import com.madebyjoaogomes.portfolio.project.ProjectOrderRequest;
import com.madebyjoaogomes.portfolio.project.ProjectRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class AuthorizationIT extends IntegrationTest {

    @Test
    void anonymousReadsPublishedContent() throws Exception {
        mvc.perform(get("/public/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").value(1));
    }

    @Test
    void anonymousCannotReachTheAdminApi() throws Exception {
        mvc.perform(get("/admin/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").value("/admin/projects"));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        var token = token(VIEWER);
        mvc.perform(get("/admin/projects").header(HttpHeaders.AUTHORIZATION, "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenCarriesTheAccountRole() throws Exception {
        mvc.perform(withJson(post("/auth/token"), Map.of("username", VIEWER, "password", passwordOf(VIEWER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.roles[0]").value("VIEWER"));
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(withJson(post("/auth/token"), Map.of("username", ADMIN, "password", UUID.randomUUID().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Credenciais inválidas"));
    }

    @Test
    void repeatedFailuresBlockTheAddress() throws Exception {
        // Its own address, so the shared limiter does not affect the other tests' logins.
        RequestPostProcessor attacker = request -> {
            request.setRemoteAddr("203.0.113.7");
            return request;
        };
        var wrong = Map.of("username", ADMIN, "password", UUID.randomUUID().toString());
        for (int attempt = 0; attempt < 10; attempt++) {
            mvc.perform(withJson(post("/auth/token"), wrong).with(attacker)).andExpect(status().isUnauthorized());
        }

        mvc.perform(withJson(post("/auth/token"), Map.of("username", ADMIN, "password", passwordOf(ADMIN))).with(attacker))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        token(ADMIN);
    }

    @Test
    void accountWithoutPasswordIsDisabled() throws Exception {
        mvc.perform(withJson(post("/auth/token"), Map.of("username", "desativado", "password", UUID.randomUUID().toString())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewerReadsTheAdminApi() throws Exception {
        mvc.perform(as(VIEWER, get("/admin/projects"))).andExpect(status().isOk());
        mvc.perform(as(VIEWER, get("/admin/articles"))).andExpect(status().isOk());
    }

    @Test
    void viewerCannotWrite() throws Exception {
        var request = new ProjectRequest("novo", "Novo", "Categoria", "/thumb.png", null,
                TestData.summary(), TestData.details(), TestData.seo());

        mvc.perform(withJson(as(VIEWER, post("/admin/projects")), request))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(withJson(as(VIEWER, put("/admin/projects/order")), new ProjectOrderRequest(List.of(1L))))
                .andExpect(status().isForbidden());
        mvc.perform(as(VIEWER, post("/admin/articles/1/publish"))).andExpect(status().isForbidden());
        mvc.perform(as(VIEWER, delete("/admin/articles/1"))).andExpect(status().isForbidden());
    }

    @Test
    void adminWrites() throws Exception {
        var request = new ProjectRequest("novo", "Novo", "Categoria", "/thumb.png", null,
                TestData.summary(), TestData.details(), TestData.seo());

        mvc.perform(withJson(as(ADMIN, post("/admin/projects")), request)).andExpect(status().isCreated());
    }

    @Test
    void unknownRoutesAreClosed() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }
}
