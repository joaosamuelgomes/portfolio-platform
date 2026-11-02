package com.madebyjoaogomes.portfolio.site;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.madebyjoaogomes.portfolio.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** The test profile has no SITE_DEPLOY_HOOK_URL; the hook call itself is covered by SiteDeployServiceTest. */
class SiteApiIT extends IntegrationTest {

    @Test
    void viewerSeesWhetherTheDeployIsConfigured() throws Exception {
        mvc.perform(as(VIEWER, get("/admin/site")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deployConfigured").value(false))
                .andExpect(jsonPath("$.lastDeployRequestedAt").doesNotExist());
    }

    @Test
    void viewerCannotDeploy() throws Exception {
        mvc.perform(as(VIEWER, post("/admin/site/deploy"))).andExpect(status().isForbidden());
    }

    @Test
    void deployWithoutAHookIsUnavailable() throws Exception {
        mvc.perform(as(ADMIN, post("/admin/site/deploy")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Atualização do site desativada"));
    }
}
