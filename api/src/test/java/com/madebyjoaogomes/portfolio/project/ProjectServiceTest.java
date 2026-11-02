package com.madebyjoaogomes.portfolio.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.madebyjoaogomes.portfolio.TestData;
import com.madebyjoaogomes.portfolio.content.ContentNotFoundException;
import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import com.madebyjoaogomes.portfolio.content.SlugConflictException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projects;

    @InjectMocks
    private ProjectService service;

    @Test
    void createsDraftAtTheEndOfTheOrder() {
        when(projects.existsBySlug("novo")).thenReturn(false);
        when(projects.findMaxDisplayOrder()).thenReturn(3);
        when(projects.saveAndFlush(any())).then(returnsFirstArg());

        var created = service.create(request("novo"));

        assertThat(created.order()).isEqualTo(4);
        assertThat(created.status()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(created.slug()).isEqualTo("novo");
    }

    @Test
    void rejectsSlugAlreadyInUse() {
        when(projects.existsBySlug("gelatto")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("gelatto"))).isInstanceOf(SlugConflictException.class);
        verify(projects, never()).saveAndFlush(any());
    }

    @Test
    void updateAllowsKeepingItsOwnSlug() {
        var project = withId(TestData.project("gelatto", 1), 1L);
        when(projects.findById(1L)).thenReturn(Optional.of(project));
        when(projects.existsBySlugAndIdNot("gelatto", 1L)).thenReturn(false);
        when(projects.saveAndFlush(project)).thenReturn(project);

        var updated = service.update(1L, request("gelatto"));

        assertThat(updated.title()).isEqualTo("Título novo");
    }

    @Test
    void failsOnUnknownProject() {
        when(projects.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(9L))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessageContaining("9");
    }

    @Test
    void publishesAndArchives() {
        var project = withId(TestData.project("gelatto", 1), 1L);
        when(projects.findById(1L)).thenReturn(Optional.of(project));
        when(projects.saveAndFlush(project)).thenReturn(project);

        assertThat(service.publish(1L).status()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(service.archive(1L).status()).isEqualTo(PublicationStatus.ARCHIVED);
        assertThat(service.unpublish(1L).status()).isEqualTo(PublicationStatus.DRAFT);
    }

    @Test
    void reorderRewritesTheOrderFromOne() {
        var first = withId(TestData.project("a", 1), 1L);
        var second = withId(TestData.project("b", 2), 2L);
        var third = withId(TestData.project("c", 7), 3L);
        when(projects.findAll()).thenReturn(List.of(first, second, third));

        var reordered = service.reorder(List.of(3L, 1L, 2L));

        assertThat(reordered).extracting(ProjectResponse::id).containsExactly(3L, 1L, 2L);
        assertThat(reordered).extracting(ProjectResponse::order).containsExactly(1, 2, 3);
    }

    @Test
    void reorderRequiresEveryProjectExactlyOnce() {
        when(projects.findAll()).thenReturn(List.of(withId(TestData.project("a", 1), 1L), withId(TestData.project("b", 2), 2L)));

        assertThatThrownBy(() -> service.reorder(List.of(1L))).isInstanceOf(InvalidProjectOrderException.class);
        assertThatThrownBy(() -> service.reorder(List.of(1L, 1L))).isInstanceOf(InvalidProjectOrderException.class);
        assertThatThrownBy(() -> service.reorder(List.of(1L, 2L, 3L))).isInstanceOf(InvalidProjectOrderException.class);
    }

    private static ProjectRequest request(String slug) {
        return new ProjectRequest(slug, "Título novo", "Categoria", "/thumb.png", null,
                TestData.summary(), TestData.details(), TestData.seo());
    }

    private static Project withId(Project project, Long id) {
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }
}
