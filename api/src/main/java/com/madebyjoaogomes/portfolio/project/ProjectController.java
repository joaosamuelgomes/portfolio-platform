package com.madebyjoaogomes.portfolio.project;

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
@RequestMapping("/admin/projects")
@Tag(name = "Projects", description = "Cadastro, publicação e ordenação dos cases")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listProjects", summary = "Lista os projetos na ordem de exibição")
    public List<ProjectResponse> list(@RequestParam(required = false) PublicationStatus status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getProject", summary = "Busca um projeto")
    public ProjectResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createProject", summary = "Cria um projeto como rascunho, no fim da ordem")
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateProject", summary = "Substitui o conteúdo de um projeto")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteProject", summary = "Exclui um projeto")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/publish")
    @Operation(operationId = "publishProject", summary = "Publica um projeto")
    public ProjectResponse publish(@PathVariable Long id) {
        return service.publish(id);
    }

    @PostMapping("/{id}/unpublish")
    @Operation(operationId = "unpublishProject", summary = "Volta um projeto para rascunho")
    public ProjectResponse unpublish(@PathVariable Long id) {
        return service.unpublish(id);
    }

    @PostMapping("/{id}/archive")
    @Operation(operationId = "archiveProject", summary = "Arquiva um projeto")
    public ProjectResponse archive(@PathVariable Long id) {
        return service.archive(id);
    }

    @PutMapping("/order")
    @Operation(operationId = "reorderProjects", summary = "Define a ordem de exibição de todos os projetos")
    public List<ProjectResponse> reorder(@Valid @RequestBody ProjectOrderRequest request) {
        return service.reorder(request.projectIds());
    }
}
