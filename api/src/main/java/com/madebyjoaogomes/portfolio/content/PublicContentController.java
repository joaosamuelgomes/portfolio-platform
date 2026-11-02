package com.madebyjoaogomes.portfolio.content;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@Tag(name = "Public content", description = "Conteúdo publicado, lido pelo build do site")
@SecurityRequirements
public class PublicContentController {

    private final PublicContentService service;

    public PublicContentController(PublicContentService service) {
        this.service = service;
    }

    @GetMapping("/content")
    @Operation(operationId = "getPublishedContent", summary = "Snapshot com todos os projetos e artigos publicados")
    public ContentSnapshot content() {
        return service.snapshot();
    }
}
