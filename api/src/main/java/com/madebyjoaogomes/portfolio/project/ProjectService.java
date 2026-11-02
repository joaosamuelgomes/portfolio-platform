package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.ContentNotFoundException;
import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import com.madebyjoaogomes.portfolio.content.SlugConflictException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(PublicationStatus status) {
        var found = status == null
                ? projects.findAllByOrderByDisplayOrderAscIdAsc()
                : projects.findByStatusOrderByDisplayOrderAscIdAsc(status);
        return found.stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) {
        return ProjectResponse.from(find(id));
    }

    public ProjectResponse create(ProjectRequest request) {
        if (projects.existsBySlug(request.slug())) {
            throw new SlugConflictException(request.slug());
        }
        var project = new Project(projects.findMaxDisplayOrder() + 1);
        apply(project, request);
        return ProjectResponse.from(projects.saveAndFlush(project));
    }

    public ProjectResponse update(Long id, ProjectRequest request) {
        var project = find(id);
        if (projects.existsBySlugAndIdNot(request.slug(), id)) {
            throw new SlugConflictException(request.slug());
        }
        apply(project, request);
        return ProjectResponse.from(projects.saveAndFlush(project));
    }

    public void delete(Long id) {
        projects.delete(find(id));
    }

    public ProjectResponse publish(Long id) {
        return changeStatus(id, Project::publish);
    }

    public ProjectResponse unpublish(Long id) {
        return changeStatus(id, Project::unpublish);
    }

    public ProjectResponse archive(Long id) {
        return changeStatus(id, Project::archive);
    }

    /** Rewrites the display order as 1..n following {@code projectIds}, which must name every project once. */
    public List<ProjectResponse> reorder(List<Long> projectIds) {
        Set<Long> requested = new HashSet<>(projectIds);
        var all = projects.findAll().stream().collect(Collectors.toMap(Project::getId, Function.identity()));
        if (requested.size() != projectIds.size() || !requested.equals(all.keySet())) {
            throw new InvalidProjectOrderException();
        }
        for (int index = 0; index < projectIds.size(); index++) {
            all.get(projectIds.get(index)).moveTo(index + 1);
        }
        projects.flush();
        return projectIds.stream().map(all::get).map(ProjectResponse::from).toList();
    }

    private ProjectResponse changeStatus(Long id, Consumer<Project> transition) {
        var project = find(id);
        transition.accept(project);
        return ProjectResponse.from(projects.saveAndFlush(project));
    }

    private Project find(Long id) {
        return projects.findById(id).orElseThrow(() -> new ContentNotFoundException("Projeto", id));
    }

    private static void apply(Project project, ProjectRequest request) {
        project.update(request.slug(), request.title(), request.category(), request.thumbnail(), request.logo(),
                request.summary(), request.details(), request.seo());
    }
}
