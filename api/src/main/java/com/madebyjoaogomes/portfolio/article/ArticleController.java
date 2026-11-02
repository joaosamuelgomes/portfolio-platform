package com.madebyjoaogomes.portfolio.article;

import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/articles")
@Tag(name = "Articles", description = "Cadastro e publicação dos artigos em Markdown")
@SecurityRequirement(name = "bearerAuth")
public class ArticleController {

    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listArticles", summary = "Lista os artigos, mais recentes primeiro")
    public List<ArticleResponse> list(@RequestParam(required = false) PublicationStatus status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getArticle", summary = "Busca um artigo")
    public ArticleResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createArticle", summary = "Cria um artigo como rascunho")
    public ArticleResponse create(@Valid @RequestBody ArticleRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateArticle", summary = "Substitui o conteúdo de um artigo")
    public ArticleResponse update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteArticle", summary = "Exclui um artigo")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/publish")
    @Operation(operationId = "publishArticle", summary = "Publica um artigo; sem data, usa a de hoje")
    public ArticleResponse publish(@PathVariable Long id) {
        return service.publish(id);
    }

    @PostMapping("/{id}/unpublish")
    @Operation(operationId = "unpublishArticle", summary = "Volta um artigo para rascunho")
    public ArticleResponse unpublish(@PathVariable Long id) {
        return service.unpublish(id);
    }

    @PostMapping("/{id}/archive")
    @Operation(operationId = "archiveArticle", summary = "Arquiva um artigo")
    public ArticleResponse archive(@PathVariable Long id) {
        return service.archive(id);
    }
}
