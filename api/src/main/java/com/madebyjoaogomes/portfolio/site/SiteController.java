package com.madebyjoaogomes.portfolio.site;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/site")
@Tag(name = "Site", description = "Atualização do site estático que lê o conteúdo publicado")
@SecurityRequirement(name = "bearerAuth")
public class SiteController {

    private final SiteDeployService service;

    public SiteController(SiteDeployService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "getSiteStatus", summary = "Diz se o deploy do site está configurado e quando foi pedido")
    public SiteStatus status() {
        return service.status();
    }

    @PostMapping("/deploy")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "deploySite", summary = "Pede um novo build do site com o conteúdo publicado")
    public SiteStatus deploy() {
        return service.requestDeploy();
    }
}
